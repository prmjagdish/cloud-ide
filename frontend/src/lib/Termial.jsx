import React, { useEffect, useRef } from "react";
import { Terminal } from "xterm";
import "xterm/css/xterm.css";

const LinuxTerminal = () => {
  const terminalRef = useRef(null);
  const term = useRef(null);

  useEffect(() => {
    const xterm = new Terminal({
      cursorBlink: true,
      theme: {
        foreground: "#CCCCCC",
        background: "#1E1E1E",
        cursor: "#FFFFFF",
        selection: "rgba(255,255,255,0.3)",
      },
    });

    term.current = xterm;

    xterm.open(terminalRef.current);
    xterm.focus();
    xterm.writeln("Welcome to Cloud IDE!");
    xterm.write("$ ");

    let buffer = "";

    xterm.onKey(({ key, domEvent }) => {
      if (domEvent.key === "Enter") {
        xterm.write("\r\nYou typed: " + buffer + "\r\n$ ");
        buffer = "";
      } else if (domEvent.key === "Backspace") {
        if (buffer.length > 0) {
          buffer = buffer.slice(0, -1);
          xterm.write("\b \b");
        }
      } else {
        buffer += key;
        xterm.write(key);
      }
    });

    return () => {
      xterm.dispose();
    };
  }, []);

  return (
    <div
      style={{
        height: "400px",
        width: "100%",
        backgroundColor: "#1E1E1E",
        padding: "10px",
        boxSizing: "border-box",
      }}
    >
      <div
        ref={terminalRef}
        tabIndex={0}
        style={{ height: "100%", width: "100%" }}
      />
    </div>
  );
};

export default LinuxTerminal;
