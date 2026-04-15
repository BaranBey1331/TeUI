import "./index.css";
import { Composition } from "remotion";
import { TeuiPromo } from "./TeuiPromo";

export const RemotionRoot: React.FC = () => {
  return (
    <>
      <Composition
        id="TeuiPromo"
        component={TeuiPromo}
        durationInFrames={1800}
        fps={30}
        width={1080}
        height={1920}
      />
    </>
  );
};
