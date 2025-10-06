#pragma once
#include <atomic>
#include <cstdint>
#include <cstring>

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
