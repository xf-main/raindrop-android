# Raindrop.io Android app
[<img src="http://pluspng.com/img-png/get-it-on-google-play-badge-png-open-2000.png" height="48">](https://play.google.com/store/apps/details?id=io.raindrop.raindropio)

Official Android client for Raindrop.io. This repository does not include any credentials or other sensitive information.

I decided to open-source it to be more transparent with users about what exactly the app does on your device, and so that anyone can follow the development progress.

I am open to all kinds of contributions. If you find a bug or have an improvement in mind, feel free to submit an issue or a pull request!

##### App features:
- Truly native look and feel
- Share extension
- Sign in with Google or Apple
- Theme support
- Multilingual UI

##### Folder structure:
- src
    - *assets* — static files
    - *co* — common React components
    - *data* — Redux store (this code is also shared with the Raindrop.io web app)
    - *local* — Redux store specific to this app
    - *modules* — navigation, i18n, etc.
    - *screens*

## Install
1. Rename `.env.example` to `.env`
2. `npm i`
3. `react-native run-android`