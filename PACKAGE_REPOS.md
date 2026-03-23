# Consulo Plugin Package Repositories

Native package manager repositories for installing and updating Consulo IDE and its plugins.

## Channels

Choose a channel based on your stability preference:

| Channel | Cadence | Use case |
|---------|---------|----------|
| `release` | Monthly | **Recommended** — stable builds |
| `beta` | Weekly | Pre-release testing |
| `alpha` | Daily | Early access |
| `nightly` | Per commit | Bleeding edge |
| `valhalla` | Snapshot | Platform SNAPSHOT builds |

**Important:** each channel is independent. A plugin only appears in a channel once it has been
explicitly deployed to that channel. If a plugin is missing from `release`, it means it has not
been promoted there yet — switch to `beta` or `alpha` to access it sooner.
Channels are not cumulative: `release` does **not** automatically include everything from `nightly`.

Replace `{channel}` in the examples below with your chosen channel name.

---

## APT — Debian / Ubuntu / Linux Mint

Supports architectures: `amd64`, `arm64`, `i386`, `riscv64`, `loong64`.

### Add repository

```bash
echo "deb [trusted=yes] https://api.consulo.io/apt {channel} main" \
  | sudo tee /etc/apt/sources.list.d/consulo.list
sudo apt update
```

Example (release channel):
```bash
echo "deb [trusted=yes] https://api.consulo.io/apt release main" \
  | sudo tee /etc/apt/sources.list.d/consulo.list
sudo apt update
```

> `trusted=yes` is required until GPG signing is set up.

### Install the IDE

```bash
# With bundled JDK (recommended)
sudo apt install consulo-with-jdk

# Without bundled JDK (uses system Java)
sudo apt install consulo-without-jdk
```

### Install a plugin

```bash
sudo apt install consulo-plugin-com.intellij.git
```

Plugin packages declare a dependency on `consulo-with-jdk | consulo-without-jdk`,
so installing any plugin will pull in the IDE if it is not already present.

### Update IDE and all plugins

```bash
sudo apt upgrade "consulo*"
```

### Update plugins only

```bash
sudo apt upgrade "consulo-plugin-*"
```

### Remove a plugin

```bash
sudo apt remove consulo-plugin-com.intellij.git
```

---

## DNF / YUM — Fedora / RHEL / CentOS

### Add repository

Create `/etc/yum.repos.d/consulo.repo`:

```ini
[consulo-release]
name=Consulo Plugin Repository (release)
baseurl=https://api.consulo.io/rpm/release
enabled=1
gpgcheck=0
```

Or for a different channel, e.g. beta:

```ini
[consulo-beta]
name=Consulo Plugin Repository (beta)
baseurl=https://api.consulo.io/rpm/beta
enabled=1
gpgcheck=0
```

### Install the IDE

```bash
sudo dnf install consulo-with-jdk
```

### Install a plugin

```bash
sudo dnf install consulo-plugin-com.intellij.git
```

### Update IDE and all plugins

```bash
sudo dnf upgrade "consulo*"
```

---

## Zypper — openSUSE

### Add repository

```bash
sudo zypper addrepo --no-gpgcheck \
  https://api.consulo.io/rpm/release \
  consulo-release
sudo zypper refresh
```

### Install a plugin

```bash
sudo zypper install consulo-plugin-com.intellij.git
```

### Update all Consulo packages

```bash
sudo zypper update consulo*
```

---

## Pacman — Arch Linux / Manjaro

### Add repository

Add to `/etc/pacman.conf` (before the `[extra]` section):

```ini
[consulo-release]
Server = https://api.consulo.io/pacman/release
SigLevel = Never
```

Then sync:

```bash
sudo pacman -Sy
```

> `SigLevel = Never` is required until package signing is set up.

### Install the IDE

```bash
sudo pacman -S consulo-with-jdk
```

### Install a plugin

```bash
sudo pacman -S consulo-plugin-com.intellij.git
```

Both `consulo-with-jdk` and `consulo-without-jdk` provide the virtual package `consulo`,
which plugin packages depend on.

### Update IDE and all plugins

```bash
sudo pacman -Syu
```

---

## Homebrew — macOS

Formulas are hosted in per-channel tap repositories on GitHub.

### Add tap

```bash
brew tap consulo/release
```

Homebrew infers the URL from the tap name (`consulo/release` → `https://github.com/consulo/homebrew-release`).
You can also specify it explicitly:

```bash
brew tap consulo/release https://github.com/consulo/homebrew-release
```

For other channels (replace `release` with `beta`, `alpha`, or `nightly`):
```bash
brew tap consulo/nightly
# resolves to https://github.com/consulo/homebrew-nightly
```

### Install the IDE

```bash
# With bundled JDK (recommended)
brew install consulo-with-jdk

# Without bundled JDK (uses system Java)
brew install consulo-without-jdk
```

### Install a plugin

```bash
brew install consulo-plugin-com.intellij.git
```

### Update IDE and all plugins

```bash
brew upgrade --greedy "consulo*"
```

### Direct install (without adding a tap)

Formula files can be installed directly by URL without adding a tap:

```bash
brew install https://api.consulo.io/homebrew/release/formula/consulo-plugin-com.intellij.git.rb
```
---

## WinGet — Windows

Consulo is available as a WinGet REST source, covering both the IDE and plugins.

### Add source

```powershell
winget source add --name consulo --arg https://api.consulo.io/winget/{channel} --type Microsoft.Rest
```

Example (release channel):
```powershell
winget source add --name consulo --arg https://api.consulo.io/winget/release --type Microsoft.Rest
```

### Install the IDE

```powershell
winget install consulo.with-jdk
```

### Install a plugin

```powershell
winget install consulo.plugin-com.intellij.git
```

### Update IDE and all plugins

```powershell
winget upgrade --source consulo --all
```

### Remove source

```powershell
winget source remove consulo
```

---

## Platform package names

| Linux/macOS package | WinGet ID | Description |
|---------------------|-----------|-------------|
| `consulo-with-jdk` | `consulo.with-jdk` | Consulo IDE with bundled JDK (recommended) |
| `consulo-without-jdk` | `consulo.without-jdk` | Consulo IDE without bundled JDK (requires system Java 21+) |
| `consulo-plugin-{id}` | `consulo.plugin-{id}` | Plugin with the given plugin ID |

Plugin packages declare a dependency on `consulo-with-jdk` **or** `consulo-without-jdk`,
so installing any plugin will automatically pull in the IDE if not already installed.

## Plugin naming convention

Plugin IDs map directly to package names:

```
Plugin ID:    com.intellij.git
Package name: consulo-plugin-com.intellij.git

Plugin ID:    org.jetbrains.plugins.gradle
Package name: consulo-plugin-org.jetbrains.plugins.gradle
```