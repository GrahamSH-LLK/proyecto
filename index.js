// create websocket server with uwebsockets
import Gameboy from "serverboy";
import fs from "fs";
const emulator = new Gameboy();
const zelda = fs.readFileSync("zelda.gb");
const tetris = fs.readFileSync("tetris.gb");
const mario = fs.readFileSync("mario.gb");
const pocketlove = fs.readFileSync("pocketlove.gb");

const twenty48 = fs.readFileSync("2048.gb");
import { Writable } from "stream";
emulator.loadRom(zelda);

import uWS from "uWebSockets.js";
let pressedKeys = [];
import { AudioContext } from "web-audio-api";
import Speaker from "speaker";

//const context = new AudioContext();

const speaker = new Speaker({
  channels: 1,
  bitDepth: 32,

  sampleRate: 44150.56842105263,
    float: true,
});
/*const floatTo16BitPCM = (value) => {
  const s = Math.max(-1, Math.min(1, value)); // Clamp value to -1 to 1
  return s < 0 ? s * 0x8000 : s * 0x7fff;
};
*/
// Create a writable stream to process the audio data
const audioStream = new Writable({
  write(chunk, encoding, callback) {
    // Create a buffer to hold the converted PCM data
    const pcmBuffer = Buffer.alloc(chunk.length * 4); // 2 bytes per sample (16-bit)

    // Convert the floating-point samples to 16-bit PCM
    for (let i = 0; i < chunk.length; i++) {
      pcmBuffer.writeFloatBE((chunk[i]), i * 2);
    }

    // Write the PCM buffer to the speaker
    speaker.write(pcmBuffer);
    callback();
  },
  decodeStrings: false
});


const app = uWS
  .App()
  .ws("/*", {
    /* Options */
    compression: uWS.SHARED_COMPRESSOR,
    maxPayloadLength: 16 * 1024 * 1024,
    idleTimeout: 10,
    /* Handlers */
    open: (ws, req) => {
      console.log("A WebSocket connected!");
      let frame = 0;
      setInterval(() => {
        emulator.pressKeys(pressedKeys);

        let currScreen = emulator.doFrame();
        let currentAudio = emulator.getAudio();
        let audioLoop = [];
        for (let i = 0; i < 705; i += 2) {
          audioLoop.push(currentAudio[i]);
        }
        console.log("Audio loop", audioLoop.length);
        // array to arraybuffer
        audioStream.write(Buffer.from(new Float32Array(audioLoop).buffer));
        if (frame % 4 === 0) {
          try {
            //console.log("Sending frame");
            ws.send(
              JSON.stringify(currScreen) //+ "\n" + JSON.stringify(audioLoop)
            );
          } catch {
            clearInterval();
          }
        }
        frame++;
      }, 1000 / 60);
    },
    message: (ws, message, isBinary) => {
      // arraybuffer to string
      let parsedMessage = JSON.parse(new TextDecoder().decode(message));

      if (parsedMessage.event === "loadRom") {
        console.log("Loading rom", parsedMessage.rom);
        if (parsedMessage.rom === "zelda") {
          emulator.loadRom(zelda);
        } else if (parsedMessage.rom === "tetris") {
          emulator.loadRom(tetris);
        } else if (parsedMessage.rom === "mario") {
          emulator.loadRom(mario);
        } else if (parsedMessage.rom === "2048") {
          emulator.loadRom(twenty48);
        } else if (parsedMessage.rom === "pocketlove") {
          emulator.loadRom(pocketlove);
        }
      }
      if (
        parsedMessage.event === "keypress" ||
        parsedMessage.event === "keyrelease"
      ) {
        console.log("Key pressed", parsedMessage.key);
        // keycode to key
        switch (parsedMessage.key) {
          case 38:
            parsedMessage.key = Gameboy.KEYMAP.UP;
            break;
          case 40:
            parsedMessage.key = Gameboy.KEYMAP.DOWN;
            break;
          case 37:
            parsedMessage.key = Gameboy.KEYMAP.LEFT;
            break;
          case 39:
            parsedMessage.key = Gameboy.KEYMAP.RIGHT;
            break;
          // enter
          case 10:
            parsedMessage.key = Gameboy.KEYMAP.START;
            break;
          // space
          case 32:
            parsedMessage.key = Gameboy.KEYMAP.SELECT;
            break;
          case 65:
            parsedMessage.key = Gameboy.KEYMAP.A;
            break;
          case 66:
            parsedMessage.key = Gameboy.KEYMAP.B;
            break;
          default:
            return;
        }
        if (parsedMessage.event === "keyrelease") {
          pressedKeys = pressedKeys.filter((key) => key !== parsedMessage.key);
          return;
        }
        pressedKeys.push(parsedMessage.key);
      }
    },
    close: (ws, code, message) => {
      console.log("WebSocket closed");
    },
  })
  .listen(9002, (listenSocket) => {
    if (listenSocket) {
      console.log("Listening to port 9001");
    }
  });
