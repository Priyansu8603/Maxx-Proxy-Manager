# Maxx Proxy Manager

**Version:** 2.0
**Last Updated:** February 7, 2026

---

## 📱 Overview

Maxx Proxy Manager is a modern Android application for managing, testing, and monitoring proxy connections. Built with **Jetpack Compose**, **Clean Architecture**, and **MVVM**, this app provides a simple yet powerful interface for proxy management without the complexity of VPN implementations.

### Key Features

✅ **Proxy Management**
- Add, edit, and delete HTTP/SOCKS5 proxies
- Organize proxies by location/category
- Mark favorites for quick access
- Bulk operations (multi-select delete)

✅ **Connectivity Testing**
- Test proxy connectivity with one tap
- Measure latency (response time)
- Real-time connection status
- IPv4/IPv6 support testing

✅ **Geo-Location Integration**
- Automatic country/city detection
- ISP identification
- Flag display for visual identification

✅ **Import/Export**
- Export proxy lists to text files
- Import functionality (coming in Phase 2)

✅ **Modern UI**
- Material 3 design
- Light/Dark theme support
- Smooth animations
- Multi-language support (EN, ES, HI)

---

## 🏗️ Architecture

This project follows **Clean Architecture** principles with clear separation of concerns:

```
Presentation Layer (Compose UI + ViewModel)
        ↓
Domain Layer (UseCases + Models) [Phase 2]
        ↓
Data Layer (Repository + Room Database)
```

For detailed architecture documentation, see [ARCHITECTURE.md](ARCHITECTURE.md).

---

## 🚀 Recent Changes (v2.0)

## 📦 Tech Stack

### Core
- **Language:** Kotlin 2.0.0
- **UI Framework:** Jetpack Compose
- **Architecture:** MVVM + Clean Architecture
- **Dependency Injection:** Hilt

### Libraries
- **Database:** Room 2.7.1
- **Networking:** OkHttp 4.12.0
- **Preferences:** DataStore
- **Image Loading:** Coil 2.4.0
- **WebView:** AndroidX WebKit 1.11.0

---

## 🛠️ Building the Project

### Prerequisites
- Android Studio Hedgehog | 2023.1.1 or later
- JDK 11 or higher
- Android SDK 24 (minSdk) to 34 (targetSdk)

### Steps
1. Clone the repository
   ```bash
   git clone <repository-url>
   cd maxx-proxy-manager
   ```

2. Open in Android Studio
   ```
   File → Open → Select project folder
   ```

3. Sync Gradle
   ```
   File → Sync Project with Gradle Files
   ```

4. Run the app
   ```
   Run → Run 'app'
   ```

---

## 📱 Screenshots

_(Coming soon)_


## 🤝 Contributing

Contributions are welcome! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Follow Kotlin coding conventions
4. Write meaningful commit messages
5. Add tests for new features
6. Submit a pull request

### Commit Message Format
```
<type>: <description>

<body>
```

Types: `feat`, `fix`, `refactor`, `docs`, `test`, `chore`

---


## 📞 Support

For questions or issues:
- Open an issue on GitHub
- Check the [ARCHITECTURE.md](ARCHITECTURE.md) for design decisions

---

## 📊 Project Stats

| Metric | Value |
|--------|-------|
| Language | Kotlin |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 34 (Android 14) |
| Lines of Code | ~2,600 |
| Build Time | ~25s |
| APK Size | ~4 MB |

---

## 🎯 Design Principles

1. **Simplicity** - No unnecessary complexity
2. **Clean Architecture** - Clear separation of concerns
3. **Testability** - All layers independently testable
4. **Modern Android** - Compose, Coroutines, Flow
5. **User-Centric** - Intuitive UI/UX

---

**Made with ❤️ using Kotlin & Jetpack Compose**

## Add your files

- [ ] [Create](https://docs.gitlab.com/ee/user/project/repository/web_editor.html#create-a-file) or [upload](https://docs.gitlab.com/ee/user/project/repository/web_editor.html#upload-a-file) files
- [ ] [Add files using the command line](https://docs.gitlab.com/topics/git/add_files/#add-files-to-a-git-repository) or push an existing Git repository with the following command:

```
cd existing_repo
git remote add origin https://milabx.com/clients/maxx-proxy-manager.git
git branch -M main
git push -uf origin main
```

## Integrate with your tools

- [ ] [Set up project integrations](https://milabx.com/clients/maxx-proxy-manager/-/settings/integrations)

## Collaborate with your team

- [ ] [Invite team members and collaborators](https://docs.gitlab.com/ee/user/project/members/)
- [ ] [Create a new merge request](https://docs.gitlab.com/ee/user/project/merge_requests/creating_merge_requests.html)
- [ ] [Automatically close issues from merge requests](https://docs.gitlab.com/ee/user/project/issues/managing_issues.html#closing-issues-automatically)
- [ ] [Enable merge request approvals](https://docs.gitlab.com/ee/user/project/merge_requests/approvals/)
- [ ] [Set auto-merge](https://docs.gitlab.com/user/project/merge_requests/auto_merge/)

## Test and Deploy

Use the built-in continuous integration in GitLab.

- [ ] [Get started with GitLab CI/CD](https://docs.gitlab.com/ee/ci/quick_start/)
- [ ] [Analyze your code for known vulnerabilities with Static Application Security Testing (SAST)](https://docs.gitlab.com/ee/user/application_security/sast/)
- [ ] [Deploy to Kubernetes, Amazon EC2, or Amazon ECS using Auto Deploy](https://docs.gitlab.com/ee/topics/autodevops/requirements.html)
- [ ] [Use pull-based deployments for improved Kubernetes management](https://docs.gitlab.com/ee/user/clusters/agent/)
- [ ] [Set up protected environments](https://docs.gitlab.com/ee/ci/environments/protected_environments.html)

***
