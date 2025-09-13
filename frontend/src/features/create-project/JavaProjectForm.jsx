import React, { useState } from "react";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";

const JavaProjectForm = ({ onClose }) => {
  
  const [projectName, setProjectName] = useState("");
  const [buildTool, setBuildTool] = useState("maven");

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!projectName.trim()) return;
    onClose();
  };

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-md text-white border-gray-800 bg-[#252526]">
        <DialogHeader>
          <DialogTitle>Create Java Project</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-4">
          {/* Project Name */}
          <div className="space-y-2">
            <Label htmlFor="projectName">Project Name</Label>
            <Input className="border-gray-600"
              id="projectName"
              value={projectName}
              onChange={(e) => setProjectName(e.target.value)}
              placeholder="Enter project name"
              required
            />
          </div>

          {/* Build Tool */}
          <div className="space-y-2">
            <Label>Build Tool</Label>
            <RadioGroup
              value={buildTool}
              onValueChange={setBuildTool}
              className="flex gap-6"
            >
              <div className="flex items-center space-x-2">
                <RadioGroupItem value="maven" id="maven" />
                <Label htmlFor="maven">Maven</Label>
              </div>
              <div className="flex items-center space-x-2">
                <RadioGroupItem value="gradle" id="gradle" />
                <Label htmlFor="gradle">Gradle</Label>
              </div>
            </RadioGroup>
          </div>

          {/* Actions */}
          <DialogFooter className="flex justify-end gap-2">
            <Button onClick={onClose} className="bg-[#252526] hover:bg-[#333333]  font-medium py-2 px-4 border border-gray-600 rounded-md transition-colors duration-200">
              Cancel
            </Button>{" "}
            <Button
              
              className="bg-[#252526] hover:bg-[#333333]  font-medium py-2 px-4 border border-gray-600 rounded-md transition-colors duration-200"
            >
              Create
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default JavaProjectForm;
