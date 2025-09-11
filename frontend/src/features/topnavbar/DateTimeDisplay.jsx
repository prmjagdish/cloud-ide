import React, { useState, useEffect } from "react";

const DateTimeDisplay = () => {
  const [dateTime, setDateTime] = useState(new Date());

  useEffect(() => {
    const timer = setInterval(() => setDateTime(new Date()), 1000);
    return () => clearInterval(timer);
  }, []);

  return (
    <div className="text-sm font-mono">
      {dateTime.toLocaleDateString()} {dateTime.toLocaleTimeString()}
    </div>
  );
};

export default DateTimeDisplay;
