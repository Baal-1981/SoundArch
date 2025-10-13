#pragma once
#include <atomic>
#include <cstdint>
#include <cstring>

// ==============================================================================
// 🔒 LOCK-FREE SINGLE-PRODUCER SINGLE-CONSUMER (SPSC) RING BUFFER
// ==============================================================================
//
// Thread Safety Guarantees:
//   - SPSC only: ONE producer thread (audio input), ONE consumer (audio output)
//   - Lock-free: Uses std::atomic with acquire/release memory ordering
//   - Wait-free: push/pop never block, return false if full/empty
//
// Memory Ordering:
//   - head.load(memory_order_acquire): Ensures consumer sees all data writes
//   - tail.load(memory_order_acquire): Ensures producer sees all data reads
//   - head.store(memory_order_release): Publishes new data to consumer
//   - tail.store(memory_order_release): Publishes freed space to producer
//
// Usage Pattern (OboeEngine):
//   - Producer: inputStream→read() → ringBuffer.push()  (onAudioReady thread)
//   - Consumer: ringBuffer.pop() → DSP chain → output   (onAudioReady thread)
//   - Both operations run on same thread, but logically producer/consumer
//
// Performance:
//   - Power-of-two size enables fast modulo via bitwise AND: (index & (N-1))
//   - No divisions, no mutexes, minimal cache line ping-pong
//   - Typical latency: <1μs for 4096-sample buffer
//
// ==============================================================================

template<typename T, size_t N>
class RingBuffer {
    static_assert((N & (N - 1)) == 0, "Size must be power of two");

    T buffer[N];
    std::atomic<size_t> head{0};
    std::atomic<size_t> tail{0};

public:
    size_t capacity() const { return N; }

    size_t availableToWrite() const {
        return N - (head.load(std::memory_order_acquire) - tail.load(std::memory_order_acquire));
    }

    size_t availableToRead() const {
        return head.load(std::memory_order_acquire) - tail.load(std::memory_order_acquire);
    }

    bool push(const T* data, size_t count) noexcept {
        if (availableToWrite() < count) return false;
        size_t localHead = head.load(std::memory_order_relaxed);
        for (size_t i = 0; i < count; ++i) {
            buffer[(localHead + i) & (N - 1)] = data[i];
        }
        head.store(localHead + count, std::memory_order_release);
        return true;
    }

    bool pop(T* out, size_t count) noexcept {
        if (availableToRead() < count) return false;
        size_t localTail = tail.load(std::memory_order_relaxed);
        for (size_t i = 0; i < count; ++i) {
            out[i] = buffer[(localTail + i) & (N - 1)];
        }
        tail.store(localTail + count, std::memory_order_release);
        return true;
    }
};
