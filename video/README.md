# TeUI Promo Video (Remotion)

Bu klasör TeUI için gerçekçi tanıtım videosunu içerir.

## Kompozisyon

- `TeuiPromo`
- 1080x1920
- 30 FPS
- ~60 saniye

## Sahne akışı

1. TeUI konumlandırma (Termux + görsel UI)
2. CLI/TUI problem çerçevesi
3. Uygulamadan komut çalıştırma ve çıktı paneli
4. Dosya/resim yükleme ve önizleme
5. Sadece arm64-v8a APK build vurgusu
6. MVP kapanış

## Kurulum

```bash
npm install
```

## Seslendirme üretimi

```bash
npm run voiceover
```

Seslendirme dosyaları `public/voiceover/` altına yazılır.

## Render komutları

Bu ortamda `os.networkInterfaces()` çağrısı hata verdiği için Remotion çalıştırırken network mock preload kullanılıyor.

```bash
NODE_OPTIONS="--require ./scripts/mock-network.js" npx remotion still src/index.ts TeuiPromo out/teui-still.png --frame=30 --scale=0.25
NODE_OPTIONS="--require ./scripts/mock-network.js" npx remotion render src/index.ts TeuiPromo out/teui-promo.mp4
```

## Altyazı

- `public/captions-tr.json`
- Format: `@remotion/captions` `Caption[]`
