# TeUI

TeUI (Termux UI), Termux benzeri komut deneyimini daha görsel bir mobil arayüzde sunan bir MVP projedir.

## MVP kapsamı

- Uygulama içinden komut çalıştırma (stdout/stderr çıktısıyla)
- Görsel terminal/log paneli
- Dosya veya resim seçme
- Resimler için önizleme
- Sadece `arm64-v8a` APK üreten GitHub Actions build workflow
- Remotion ile hazırlanmış tanıtım videosu

## Android proje dosyaları

- `app/` altında Jetpack Compose tabanlı Android uygulaması
- `app/build.gradle.kts` içinde ABI kısıtı yalnız `arm64-v8a`
- `.github/workflows/apk-build.yml` içinde release APK build + arm64 doğrulaması

## Çalıştırma

> Not: Bu depoda Gradle wrapper henüz ekli değil. Yerelde `gradle` komutu kurulu olmalı.

```bash
gradle :app:assembleDebug
gradle :app:assembleRelease
```

Release çıktısı `app/build/outputs/apk/release/` altında oluşur ve yalnız arm64-v8a olmalıdır.

## Remotion tanıtım videosu

Video projesi `video/` klasöründedir.

```bash
cd video
npm install
npm run voiceover
NODE_OPTIONS="--require ./scripts/mock-network.js" npx remotion still src/index.ts TeuiPromo out/teui-still.png --frame=30 --scale=0.25
NODE_OPTIONS="--require ./scripts/mock-network.js" npx remotion render src/index.ts TeuiPromo out/teui-promo.mp4
```

- Kompozisyon: `TeuiPromo`
- Hedef: 1080x1920, 30fps, yaklaşık 60s
- Sahne akışı: problem → komut çalıştırma → dosya/resim yükleme → arm64 APK build vurgusu
