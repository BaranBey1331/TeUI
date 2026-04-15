import React, { useCallback, useEffect, useMemo, useState } from "react";
import {
  AbsoluteFill,
  Audio,
  Easing,
  Img,
  Sequence,
  Series,
  interpolate,
  spring,
  staticFile,
  useCurrentFrame,
  useDelayRender,
  useVideoConfig,
} from "remotion";
import type { Caption } from "@remotion/captions";
import { CaptionsOverlay } from "./CaptionsOverlay";

type SceneCardProps = {
  title: string;
  subtitle: string;
  bullets?: string[];
  terminalLines?: string[];
  image?: string;
  accent?: string;
};

const bg = "#070B11";
const card = "#101826";
const text = "#E8F0FF";
const sub = "#9CB2D1";
const green = "#78F9A0";
const cyan = "#69D2FF";

const styles = {
  frame: {
    backgroundColor: bg,
    fontFamily:
      "Inter, SF Pro Display, system-ui, -apple-system, Segoe UI, Roboto, sans-serif",
    color: text,
  } as React.CSSProperties,
  gradientGlow: {
    position: "absolute",
    width: 620,
    height: 620,
    borderRadius: 999,
    filter: "blur(70px)",
    opacity: 0.35,
    background:
      "radial-gradient(circle at center, rgba(82,166,255,0.85) 0%, rgba(82,166,255,0.10) 50%, rgba(82,166,255,0) 75%)",
  } as React.CSSProperties,
  card: {
    width: 900,
    minHeight: 1220,
    borderRadius: 36,
    backgroundColor: card,
    boxShadow: "0 20px 80px rgba(0,0,0,0.45)",
    border: "1px solid rgba(180,220,255,0.18)",
    padding: 48,
    display: "flex",
    flexDirection: "column",
    gap: 24,
  } as React.CSSProperties,
  h1: {
    fontSize: 74,
    fontWeight: 800,
    lineHeight: 1.05,
    margin: 0,
    letterSpacing: -1,
  } as React.CSSProperties,
  h2: {
    fontSize: 58,
    fontWeight: 780,
    lineHeight: 1.1,
    margin: 0,
    letterSpacing: -0.5,
  } as React.CSSProperties,
  subtitle: {
    fontSize: 34,
    lineHeight: 1.3,
    color: sub,
    margin: 0,
    maxWidth: 760,
  } as React.CSSProperties,
  pill: {
    alignSelf: "flex-start",
    fontSize: 24,
    padding: "10px 18px",
    borderRadius: 999,
    backgroundColor: "rgba(93,186,255,0.16)",
    color: "#CBE9FF",
    border: "1px solid rgba(120,195,255,0.35)",
  } as React.CSSProperties,
  bulletWrap: {
    display: "flex",
    flexDirection: "column",
    gap: 14,
    marginTop: 6,
  } as React.CSSProperties,
  bulletRow: {
    display: "flex",
    alignItems: "center",
    gap: 12,
    fontSize: 31,
    color: "#D9E9FF",
  } as React.CSSProperties,
  terminal: {
    marginTop: 10,
    borderRadius: 24,
    background: "#060A10",
    border: "1px solid rgba(145,180,220,0.26)",
    padding: 24,
    display: "flex",
    flexDirection: "column",
    gap: 10,
  } as React.CSSProperties,
  terminalHeader: {
    fontSize: 24,
    color: "#9CB2D1",
  } as React.CSSProperties,
  terminalLine: {
    margin: 0,
    fontFamily: "JetBrains Mono, Menlo, ui-monospace, SFMono-Regular, monospace",
    fontSize: 26,
    lineHeight: 1.35,
    color: green,
    whiteSpace: "pre-wrap",
  } as React.CSSProperties,
  imagePanel: {
    marginTop: 14,
    borderRadius: 24,
    border: "1px solid rgba(145,180,220,0.26)",
    overflow: "hidden",
    backgroundColor: "#0A1220",
    height: 360,
  } as React.CSSProperties,
};

const bulletDotStyle: React.CSSProperties = {
  width: 10,
  height: 10,
  borderRadius: 99,
  backgroundColor: cyan,
  boxShadow: "0 0 18px rgba(105,210,255,0.8)",
};

const SceneCard: React.FC<SceneCardProps> = ({
  title,
  subtitle,
  bullets,
  terminalLines,
  image,
  accent = "CANLI DEMO",
}) => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();

  const enter = spring({
    frame,
    fps,
    config: {
      damping: 18,
      stiffness: 130,
    },
  });

  const opacity = interpolate(frame, [0, 9], [0, 1], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
    easing: Easing.bezier(0.16, 1, 0.3, 1),
  });

  const translateY = interpolate(enter, [0, 1], [70, 0]);
  const scale = interpolate(enter, [0, 1], [0.97, 1]);

  return (
    <AbsoluteFill
      style={{
        ...styles.frame,
        justifyContent: "center",
        alignItems: "center",
      }}
    >
      <div
        style={{
          ...styles.gradientGlow,
          top: 120,
          left: -110,
        }}
      />
      <div
        style={{
          ...styles.gradientGlow,
          bottom: 110,
          right: -120,
          transform: "scale(0.9)",
        }}
      />

      <div
        style={{
          ...styles.card,
          opacity,
          transform: `translateY(${translateY}px) scale(${scale})`,
        }}
      >
        <span style={styles.pill}>{accent}</span>
        <h2 style={styles.h2}>{title}</h2>
        <p style={styles.subtitle}>{subtitle}</p>

        {bullets && bullets.length > 0 ? (
          <div style={styles.bulletWrap}>
            {bullets.map((b) => (
              <div key={b} style={styles.bulletRow}>
                <span style={bulletDotStyle} />
                <span>{b}</span>
              </div>
            ))}
          </div>
        ) : null}

        {terminalLines && terminalLines.length > 0 ? (
          <div style={styles.terminal}>
            <div style={styles.terminalHeader}>teui-terminal · canlı çıktı</div>
            {terminalLines.map((line, idx) => {
              const lineOpacity = interpolate(frame, [idx * 8, idx * 8 + 8], [0, 1], {
                extrapolateLeft: "clamp",
                extrapolateRight: "clamp",
              });

              return (
                <p
                  key={`${line}-${idx}`}
                  style={{
                    ...styles.terminalLine,
                    opacity: lineOpacity,
                  }}
                >
                  {line}
                </p>
              );
            })}
          </div>
        ) : null}

        {image ? (
          <div style={styles.imagePanel}>
            <Img
              src={staticFile(image)}
              style={{
                width: "100%",
                height: "100%",
                objectFit: "cover",
              }}
            />
          </div>
        ) : null}
      </div>
    </AbsoluteFill>
  );
};

const IntroScene: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();

  const titleOpacity = interpolate(frame, [0, 18], [0, 1], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
    easing: Easing.bezier(0.2, 0.9, 0.2, 1),
  });

  const subtitleOpacity = interpolate(frame, [8, 28], [0, 1], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });

  const logoScale = spring({
    frame,
    fps,
    config: { damping: 16, stiffness: 120 },
  });

  return (
    <AbsoluteFill
      style={{
        ...styles.frame,
        justifyContent: "center",
        alignItems: "center",
      }}
    >
      <div
        style={{
          ...styles.gradientGlow,
          width: 760,
          height: 760,
          opacity: 0.5,
          top: 320,
          left: 190,
        }}
      />
      <div
        style={{
          textAlign: "center",
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          gap: 24,
          transform: `scale(${interpolate(logoScale, [0, 1], [0.92, 1])})`,
        }}
      >
        <div
          style={{
            width: 168,
            height: 168,
            borderRadius: 36,
            background:
              "linear-gradient(145deg, rgba(105,210,255,0.25), rgba(120,249,160,0.20))",
            border: "1px solid rgba(160,220,255,0.45)",
            boxShadow: "0 12px 50px rgba(80,160,255,0.35)",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            color: "#D8ECFF",
            fontSize: 52,
            fontWeight: 800,
          }}
        >
          TUI
        </div>

        <h1 style={{ ...styles.h1, opacity: titleOpacity }}>TeUI</h1>
        <p style={{ ...styles.subtitle, opacity: subtitleOpacity, maxWidth: 840 }}>
          Termux gücünü, mobilde daha görsel ve daha okunabilir bir deneyime
          dönüştürüyoruz.
        </p>
      </div>
    </AbsoluteFill>
  );
};

const ProblemScene: React.FC = () => (
  <SceneCard
    accent="PROBLEM"
    title="CLI güçlü, ama mobilde zor"
    subtitle="Küçük ekranda uzun komutlar ve çıktılar yönetmesi zor. TeUI bu deneyimi sadeleştirir."
    bullets={[
      "Komut girişinde daha net kontrol",
      "Çıktı panelinde okunabilir akış",
      "Terminal hissi + modern arayüz",
    ]}
  />
);

const CommandScene: React.FC = () => (
  <SceneCard
    accent="KOMUT ÇALIŞTIRMA"
    title="Uygulamadan yaz, Termux gibi çalıştır"
    subtitle="Kullanıcı komutu girer; TeUI stdout/stderr çıktısını anlık terminal paneline yazar."
    terminalLines={[
      "$ pkg update",
      "Hit:1 stable InRelease",
      "$ ls -la",
      "drwxr-xr-x  3 u0_a123 u0_a123 4096 Downloads",
      "[exit=0] 184ms",
    ]}
  />
);

const UploadScene: React.FC = () => (
  <SceneCard
    accent="DOSYA / RESİM"
    title="Dosya ve resim yükleme"
    subtitle="UI içinden dosya seç, metadata gör, resimse önizlemeyi aynı ekranda al."
    bullets={[
      "Tek dokunuşla dosya seçimi",
      "MIME türü ve boyut bilgisi",
      "Resimler için hızlı önizleme",
    ]}
    image="demo-upload.jpg"
  />
);

const BuildScene: React.FC = () => (
  <SceneCard
    accent="CI / APK"
    title="Sadece arm64-v8a APK"
    subtitle="Release pipeline yalnız arm64-v8a üretir. Gereksiz ABI çıktılarını engelleyip süreç netleşir."
    terminalLines={[
      "./gradlew :app:assembleRelease",
      "✅ teui-arm64-v8a.apk",
      "artifact: teui-arm64-v8a-apk",
    ]}
  />
);

const OutroScene: React.FC = () => (
  <SceneCard
    accent="SONRAKİ ADIM"
    title="TeUI MVP hazır"
    subtitle="İlk sürüm: komut çalıştırma + görsel terminal + dosya/resim yükleme + arm64 APK pipeline."
    bullets={[
      "Bir sonraki faz: gelişmiş TUI/PTY",
      "Çoklu oturum ve geçmiş",
      "Topluluk geri bildirimleriyle iterasyon",
    ]}
  />
);

const useCaptions = () => {
  const [captions, setCaptions] = useState<Caption[] | null>(null);
  const { delayRender, continueRender, cancelRender } = useDelayRender();
  const [handle] = useState(() => delayRender());

  const load = useCallback(async () => {
    try {
      const response = await fetch(staticFile("captions-tr.json"));
      const data = (await response.json()) as Caption[];
      setCaptions(data);
      continueRender(handle);
    } catch (err) {
      cancelRender(err);
    }
  }, [cancelRender, continueRender, handle]);

  useEffect(() => {
    load();
  }, [load]);

  return captions;
};

export const TeuiPromo: React.FC = () => {
  const { fps } = useVideoConfig();
  const captions = useCaptions();

  const sceneVoiceovers = useMemo(
    () => [
      "voiceover/scene-01-intro.wav",
      "voiceover/scene-02-problem.wav",
      "voiceover/scene-03-command.wav",
      "voiceover/scene-04-upload.wav",
      "voiceover/scene-05-build.wav",
      "voiceover/scene-06-outro.wav",
    ],
    [],
  );

  const sceneDurations = useMemo(
    () => [8 * fps, 9 * fps, 10 * fps, 9 * fps, 11 * fps, 13 * fps],
    [fps],
  );

  const sceneOffsets = useMemo(() => {
    let total = 0;
    return sceneDurations.map((duration) => {
      const start = total;
      total += duration;
      return start;
    });
  }, [sceneDurations]);

  if (!captions) {
    return null;
  }

  return (
    <AbsoluteFill style={styles.frame}>
      {sceneVoiceovers.map((src, idx) => (
        <Sequence
          key={src}
          from={sceneOffsets[idx]}
          durationInFrames={sceneDurations[idx]}
        >
          <Audio src={staticFile(src)} volume={0.95} />
        </Sequence>
      ))}

      <Series>
        <Series.Sequence durationInFrames={sceneDurations[0]}>
          <Sequence>
            <IntroScene />
          </Sequence>
        </Series.Sequence>

        <Series.Sequence durationInFrames={sceneDurations[1]}>
          <Sequence>
            <ProblemScene />
          </Sequence>
        </Series.Sequence>

        <Series.Sequence durationInFrames={sceneDurations[2]}>
          <Sequence>
            <CommandScene />
          </Sequence>
        </Series.Sequence>

        <Series.Sequence durationInFrames={sceneDurations[3]}>
          <Sequence>
            <UploadScene />
          </Sequence>
        </Series.Sequence>

        <Series.Sequence durationInFrames={sceneDurations[4]}>
          <Sequence>
            <BuildScene />
          </Sequence>
        </Series.Sequence>

        <Series.Sequence durationInFrames={sceneDurations[5]}>
          <Sequence>
            <OutroScene />
          </Sequence>
        </Series.Sequence>
      </Series>

      <CaptionsOverlay captions={captions} />
    </AbsoluteFill>
  );
};
