import { useMemo } from "react";
import type { Caption } from "@remotion/captions";
import { Sequence, useCurrentFrame, useVideoConfig } from "remotion";

const captionStyle: React.CSSProperties = {
  position: "absolute",
  left: 64,
  right: 64,
  bottom: 96,
  padding: "18px 24px",
  borderRadius: 20,
  background: "rgba(6, 10, 16, 0.74)",
  border: "1px solid rgba(156, 178, 209, 0.40)",
  fontSize: 34,
  lineHeight: 1.3,
  fontWeight: 600,
  color: "#E8F0FF",
  textAlign: "center",
};

export const CaptionsOverlay: React.FC<{ captions: Caption[] }> = ({ captions }) => {
  const { fps } = useVideoConfig();

  const items = useMemo(() => {
    return captions
      .map((caption, index) => {
        const startFrame = Math.floor((caption.startMs / 1000) * fps);
        const endFrame = Math.max(
          startFrame + 1,
          Math.ceil((caption.endMs / 1000) * fps),
        );

        return {
          id: `${index}-${caption.startMs}`,
          text: caption.text,
          startFrame,
          durationInFrames: endFrame - startFrame,
        };
      })
      .filter((item) => item.durationInFrames > 0);
  }, [captions, fps]);

  return (
    <>
      {items.map((item) => (
        <Sequence
          key={item.id}
          from={item.startFrame}
          durationInFrames={item.durationInFrames}
          layout="none"
        >
          <CaptionBubble text={item.text} />
        </Sequence>
      ))}
    </>
  );
};

const CaptionBubble: React.FC<{ text: string }> = ({ text }) => {
  const frame = useCurrentFrame();

  const opacity = Math.min(1, Math.max(0, frame / 8));
  const translateY = Math.max(0, 12 - frame * 1.5);

  return (
    <div
      style={{
        ...captionStyle,
        opacity,
        transform: `translateY(${translateY}px)`,
      }}
    >
      {text}
    </div>
  );
};
