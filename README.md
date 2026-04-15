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
- `.github/workflows/apk-build.yml` içinde release APK build + arm64 doğrulaması + imzalama (v2/v3)

## Çalıştırma

> Not: Bu depoda Gradle wrapper henüz ekli değil. Yerelde `gradle` komutu kurulu olmalı.

```bash
gradle :app:assembleDebug
gradle :app:assembleRelease
```

Release çıktısı `app/build/outputs/apk/release/` altında oluşur ve yalnız arm64-v8a olmalıdır.

## APK imzalama

CI, release APK'yı GitHub Actions secret'larından alınan keystore ile imzalar ve imza şemalarını doğrular.

Gerekli repository secrets:

- `ANDROID_KEYSTORE_BASE64` (JKS dosyasının base64 hali)
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

İmza politikası:

- v1: kapalı
- v2: açık
- v3: açık

## Remotion tanıtım videosu

Video projesi `video/` klasöründedir.

### İzleme (uygulamayı tanıtım)

- Ana video: [`video/out/teui-promo.mp4`](video/out/teui-promo.mp4)
- Kısa sürüm: [`video/out/teui-promo-short.mp4`](video/out/teui-promo-short.mp4)
- Kapak karesi: [`video/out/teui-still.png`](video/out/teui-still.png)

### Tanıtım detayları

- Kompozisyon: `TeuiPromo`
- Hedef: 1080x1920, 30fps, yaklaşık 60s
- Amaç: TeUI uygulamasını gerçek kullanım akışıyla tanıtmak
- Sahne akışı: problem → komut çalıştırma → dosya/resim yükleme → arm64 APK build vurgusu
