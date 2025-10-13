# SoundArch Documentation Index

**Last Updated:** 2025-10-13

This directory contains all centralized documentation for the SoundArch v2.0 project.

---

## 📂 Directory Structure

```
docs/
├── README.md                          # This file - documentation index
├── architecture/                      # Architecture & design documentation
├── testing/                           # Testing documentation
├── completion-reports/                # Historical completion reports
└── archived/                          # Archived planning documents
```

---

## 🏗️ Architecture Documentation

**Location:** `docs/architecture/`

### [DESIGN_SYSTEM.md](architecture/DESIGN_SYSTEM.md)
Complete UI/UX design system for SoundArch v2.0

**Contents:**
- Design principles (professional precision, immediate feedback)
- Grid & spacing system (4dp base unit)
- Typography scale (9 text styles)
- Color system (dark theme, semantic colors)
- Components (buttons, cards, sliders, meters)
- Dual UI modes (Friendly/Advanced)
- Motion & animation guidelines
- Accessibility (WCAG AA compliance)
- Audio-specific patterns (dB meters, latency display)

**Status:** ✅ Complete, implemented

### [DSP_WIRING_MATRIX.md](architecture/DSP_WIRING_MATRIX.md)
DSP parameter wiring matrix and implementation status

**Contents:**
- Complete parameter inventory (84 total parameters)
- Wiring status by module (AGC, EQ, Compressor, Limiter, NC)
- JNI bridge mapping (Kotlin ↔ C++)
- Missing features and gaps
- Implementation notes

**Status:** ✅ Complete, 88% wired

### [NAVGRAPH_MAP.md](architecture/NAVGRAPH_MAP.md)
Navigation structure and route definitions

**Contents:**
- All 18 routes (Home, Equalizer, Advanced sections)
- Navigation hierarchy and back stack behavior
- Route parameters and deeplinks
- Bottom navigation tabs
- Advanced panel structure

**Status:** ⚠️ Needs update (pre-M4 state)

---

## 🧪 Testing Documentation

**Location:** `docs/testing/`

### [DSP_TEST_SUITE.md](testing/DSP_TEST_SUITE.md)
DSP algorithm testing documentation

**Contents:**
- DSP test coverage (111 tests)
- Test breakdown by module:
  - AGC: 30 tests
  - Equalizer: 15 tests
  - Compressor: 20 tests
  - Limiter: 20 tests
  - Noise Canceller: 26 tests
- Test patterns and examples
- Running DSP tests

**Status:** ✅ Complete

### [UI_TEST_SUITE.md](testing/UI_TEST_SUITE.md)
UI and navigation testing documentation

**Contents:**
- UI test coverage (150+ tests)
- Navigation tests (24 tests)
- Component tests
- Test patterns and examples
- Running UI tests

**Status:** ✅ Complete

### [TEST_INFRASTRUCTURE_REPORT.md](testing/TEST_INFRASTRUCTURE_REPORT.md)
Test infrastructure and tooling

**Contents:**
- Test framework overview
- Test helpers and utilities
- UiIds system (test tag constants)
- Test orchestration
- CI/CD integration

**Status:** ✅ Complete

### [TEST_INFRASTRUCTURE_COMPLETE_REPORT.md](testing/TEST_INFRASTRUCTURE_COMPLETE_REPORT.md)
Final test infrastructure completion report

**Contents:**
- Test completion summary
- All tests passing (260+)
- Test execution results
- Coverage analysis

**Status:** ✅ Complete

---

## 📋 Completion Reports

**Location:** `docs/completion-reports/`

Historical milestone and phase completion reports:

- **Phase 1:** Initial DSP implementation and testing
- **Phase 2:** UI/UX development and integration tests
- **Phase 3:** Test optimization and device testing
- **M1-M4:** Milestone reports (component adoption, verification)
- **Final reports:** Test completion, infrastructure, coverage

These documents provide historical context but are not actively maintained. For current status, see [PROJECT_STATUS.md](../PROJECT_STATUS.md).

---

## 🗄️ Archived Documentation

**Location:** `docs/archived/`

Contains historical planning documents, analysis reports, and strategic plans from the development process:

- Strategic plans (PRO_STRATEGIC_PLAN, M*_STRATEGIC_PLAN)
- Progress reports (PHASE_*_PROGRESS, M*_PROGRESS)
- Analysis reports (various analysis and audit reports)
- Planning documents (PERMANENT_FIX_PLAN, TEST_TODO_PLAN)
- Inventories and checklists

These documents are kept for historical reference but are superseded by current documentation.

---

## 📖 In-Code Documentation

### Test-Specific READMEs

Located within the codebase for context-specific documentation:

#### [app/src/androidTest/README.md](../app/src/androidTest/README.md)
Comprehensive guide to instrumented tests (260+ tests)
- Running instrumented tests
- Test structure and organization
- Test patterns and best practices
- Troubleshooting guide

#### [app/src/androidTest/java/com/soundarch/ui/navigation/README.md](../app/src/androidTest/java/com/soundarch/ui/navigation/README.md)
Navigation testing system guide
- NavigationTestHelper API
- All 24 navigation tests
- Route coverage (100%)
- Navigation patterns

#### [app/src/androidTest/java/com/soundarch/tools/README.md](../app/src/androidTest/java/com/soundarch/tools/README.md)
Testing tools and utilities
- UiIdsCoverageTest (coverage scanner)
- Test helpers and utilities
- Report generation

#### [app/src/test/README.md](../app/src/test/README.md)
JVM unit tests guide (9 tests)
- Running unit tests
- Test patterns
- Mocking and coroutines
- Code coverage

#### [app/src/main/cpp/testing/README.md](../app/src/main/cpp/testing/README.md)
Golden audio test framework
- C++ test harness
- Signal generators
- Audio metrics analysis
- Regression testing

---

## 🔗 Quick Links

### Primary Documentation (Root)
- [README.md](../README.md) - Project overview, features, quick start
- [PROJECT_STATUS.md](../PROJECT_STATUS.md) - Current project status, metrics, roadmap

### Claude Code Instructions (`.claude/`)
- [claude.md](../.claude/claude.md) - Entry point and navigation
- [claude_requirements.md](../.claude/claude_requirements.md) - Requirements & checklist
- [claude_reference.md](../.claude/claude_reference.md) - Decision trees & FAQ
- [claude_phases.md](../.claude/claude_phases.md) - Phase instructions
- [claude_examples.md](../.claude/claude_examples.md) - Code examples
- [claude_final.md](../.claude/claude_final.md) - Final reminders

---

## 📊 Documentation Statistics

- **Total Documentation Files:** 20+ markdown files
- **Total Documentation Lines:** 10,000+ lines
- **Architecture Docs:** 3 files (~2,500 lines)
- **Testing Docs:** 7 files (~3,000 lines)
- **Test-Specific READMEs:** 5 files (~2,000 lines)
- **Completion Reports:** 10+ archived files
- **Historical Docs:** 60+ archived files

---

## 🎯 Documentation Standards

### Markdown Files
- **Format:** GitHub-flavored Markdown
- **Line Length:** No hard limit (readable paragraphs)
- **Headings:** Use ATX-style (`#`, `##`, etc.)
- **Code Blocks:** Specify language for syntax highlighting
- **Tables:** Use for structured data

### Status Indicators
- ✅ Complete/Passing
- ⚠️ Needs update/Partial
- ❌ Missing/Failing
- 🟢 Production-ready
- 🟡 In progress
- 🔴 Critical issue

### File Naming
- **UPPERCASE_WITH_UNDERSCORES.md** - Main documentation files
- **lowercase-with-dashes.md** - Supporting files (optional)
- **README.md** - Directory indices and guides

---

## 🔄 Maintenance

### When to Update
- **After major features:** Update PROJECT_STATUS.md, relevant architecture docs
- **After test changes:** Update test documentation and READMEs
- **After API changes:** Update DSP_WIRING_MATRIX.md if parameters change
- **After UI changes:** Update DESIGN_SYSTEM.md if patterns change
- **Quarterly:** Review and update all documentation for accuracy

### Update Process
1. Identify affected documentation files
2. Update content with current information
3. Update "Last Updated" date
4. Commit with descriptive message: `docs: update [file] - [reason]`

---

## 📞 Documentation Support

### Questions?
- Check relevant documentation section above
- Review in-code README files for specific topics
- See PROJECT_STATUS.md for current state
- Check Claude Code instructions for development guidance

### Found an Issue?
- Documentation out of date → Update and commit
- Documentation missing → Create new file in appropriate folder
- Documentation unclear → Improve clarity and examples

---

**Last Updated:** 2025-10-13
**Version:** 2.0
**Status:** ✅ Complete and organized

**End of Documentation Index**
