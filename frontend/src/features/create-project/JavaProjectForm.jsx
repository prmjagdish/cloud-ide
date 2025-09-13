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
import {
  RadioGroup,
  RadioGroupItem,
} from "@/components/ui/radio-group";
import { createProject }  from "@/utils/apis/project"; 

const JavaProjectForm = ({ onClose }) => {
  const [projectName, setProjectName] = useState("");
  const [buildTool, setBuildTool] = useState("maven");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!projectName.trim()) return;

    setLoading(true);
    try {
      console.log("funation called",projectName);
      createProject({name : projectName});
      onClose(); 
    } catch (error) {
      console.error("Failed to create project:", error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={true} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-md text-white bg-[#252526]">
        <DialogHeader>
          <DialogTitle>Create Java Project</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="projectName">Project Name</Label>
            <Input
              id="projectName"
              value={projectName}
              onChange={(e) => setProjectName(e.target.value)}
              placeholder="Enter project name"
              required
            />
          </div>

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

          <DialogFooter className="flex justify-end gap-2">
            <Button type="submit" disabled={loading}>
              {loading ? "Creating..." : "Create"}
            </Button>
            <Button type="button" variant="secondary" onClick={onClose}>
              Close
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default JavaProjectForm;
