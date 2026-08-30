# ColorWorlds – Privacy Policy Webpage

This repository hosts the official standalone, mobile-responsive **Privacy Policy** for **ColorWorlds** (Android package: `com.aistudio.colorworlds.puzzle3d`), ready for direct deployment to **GitHub Pages** and submission to the **Google Play Console**.

---

## 📁 Repository Contents

```text
ColorWorlds-Privacy-Policy/
├── index.html   # Standalone, responsive HTML5 Privacy Policy (no backend, no npm, no build needed)
└── README.md    # Instructions for GitHub upload & GitHub Pages hosting
```

---

## 🚀 How to Upload to GitHub

### Option 1: Using the GitHub Web Interface (Fastest)

1. Log into your account at [GitHub.com](https://github.com).
2. Click the **+** (plus icon) in the top-right corner and select **New repository**.
3. Name your repository (for example: `colorworlds-privacy-policy` or `colorworlds-policy`).
4. Set the repository visibility to **Public** (required for free GitHub Pages).
5. Click **Create repository**.
6. On the empty repository page, click **uploading an existing file**.
7. Drag and drop `index.html` (and optionally `README.md`) directly into the repository root.
8. Click **Commit changes**.

### Option 2: Using the Git CLI

```bash
# Initialize and commit
git init
git add index.html README.md
git commit -m "Add ColorWorlds Privacy Policy"
git branch -M main

# Add your remote and push
git remote add origin https://github.com/USERNAME/REPOSITORY-NAME.git
git push -u origin main
```

---

## 🌐 How to Enable GitHub Pages

Once your files are committed to your GitHub repository:

1. Navigate to your repository on GitHub.
2. Click on **Settings** (top navigation tab with the gear icon).
3. In the left sidebar under the **Code and automation** section, click **Pages**.
4. Under **Build and deployment** > **Source**, choose **Deploy from a branch**.
5. Under **Branch**, select `main` (or `master`) and the `/ (root)` folder.
6. Click **Save**.
7. Wait approximately 1 to 2 minutes for GitHub Pages to deploy.

---

## 🔗 Final Privacy Policy URL Format

Once deployed, your live Privacy Policy URL will be:

```text
https://USERNAME.github.io/REPOSITORY-NAME/
```

> **Example:** If your GitHub username is `mydevstudio` and your repository name is `colorworlds-policy`, your live URL will be:  
> `https://mydevstudio.github.io/colorworlds-policy/`

---

## 📋 Google Play Console Submission

1. Open the [Google Play Console](https://play.google.com/console).
2. Select your app: **ColorWorlds**.
3. In the left-hand navigation menu, scroll down to **Policy and programs** > **App content**.
4. Under the **Privacy Policy** section, click **Manage** or **Start**.
5. Paste your live GitHub Pages URL (`https://USERNAME.github.io/REPOSITORY-NAME/`).
6. Click **Save**.
