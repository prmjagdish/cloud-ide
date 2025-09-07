import React, { useEffect, useRef } from "react";
import { Terminal } from "xterm";
import "xterm/css/xterm.css";

const LinuxTerminal = () => {
  const terminalRef = useRef(null);
  const term = useRef(null);

  useEffect(() => {
    term.current = new Terminal({
      cursorBlink: true,
      theme: {
        foreground: "#CCCCCC",
        background: "#1E1E1E",
        cursor: "#FFFFFF",
        selection: "rgba(255,255,255,0.3)",
      },
    });

    term.current.open(terminalRef.current);
    term.current.writeln("Welcome to Cloud IDE!");
    term.current.writeln("$ ");

    term.current.onKey(({ key, domEvent }) => {
      if (domEvent.key === "Enter") {
        term.current.write("\r\n$ ");
      } else {
        term.current.write(key);
      }
    });
  }, []);

  return <div ref={terminalRef} style={{ height: "400px", width: "100%" }} />;
};

export default LinuxTerminal;
