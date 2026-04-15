const { execFileSync } = require("node:child_process");
const { mkdirSync, rmSync, writeFileSync } = require("node:fs");
const { join } = require("node:path");

const scenes = [
  {
    id: "scene-01-intro",
    text: "TeUI, Termux deneyimini mobilde daha görsel ve daha okunabilir hale getiriyor.",
  },
  {
    id: "scene-02-problem",
    text: "Klasik terminalde küçük ekranda komut yönetmek zor. TeUI arayüzü bu süreci sadeleştiriyor.",
  },
  {
    id: "scene-03-command",
    text: "Komutu uygulamadan yazıp çalıştırın. Standart çıktı ve hata çıktısı tek terminal panelinde görünür.",
  },
  {
    id: "scene-04-upload",
    text: "Dosya veya resim seçin. Tür ve boyut bilgisini görün. Resimler için hızlı önizleme alın.",
  },
  {
    id: "scene-05-build",
    text: "Build hattında sadece arm64 v8a APK üretilir. Böylece dağıtım akışı daha net ve kontrol edilebilir olur.",
  },
  {
    id: "scene-06-outro",
    text: "TeUI MVP, komut çalıştırma, görsel terminal ve dosya yükleme akışıyla gerçek kullanıma hazır bir temel sunuyor.",
  },
];

const projectRoot = join(__dirname, "..");
const outDir = join(projectRoot, "public", "voiceover");
const tempDir = join(projectRoot, ".tmp-voiceover");

mkdirSync(outDir, { recursive: true });
mkdirSync(tempDir, { recursive: true });

for (const scene of scenes) {
  const textPath = join(tempDir, `${scene.id}.txt`);
  const outPath = join(outDir, `${scene.id}.wav`);

  writeFileSync(textPath, scene.text, "utf8");

  execFileSync(
    "ffmpeg",
    [
      "-y",
      "-f",
      "lavfi",
      "-i",
      `flite=textfile=${textPath}:voice=slt`,
      "-ar",
      "48000",
      "-ac",
      "1",
      outPath,
    ],
    { stdio: "inherit" },
  );

  process.stdout.write(`Generated ${outPath}\n`);
}

rmSync(tempDir, { recursive: true, force: true });
process.stdout.write("Voiceover generation completed.\n");
