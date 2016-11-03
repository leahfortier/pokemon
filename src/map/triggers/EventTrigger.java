package map.triggers;

import main.Game;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Spawns DialogueSequences and adds character globals
 *
 */
public class EventTrigger extends Trigger {
	private static final Pattern dialoguePattern = Pattern.compile("(?:(dialogue):\\s*(\\w+)|(createDialogue):\\s*(\\w+)\\s+([\\s\\S]+))");
	private static final Pattern dialogueCreationPattern = Pattern.compile("text\\[(\\d+)\\]:\\s*\"([^\"]+)\"");

	// TODO: Make these all private
	public String dialogue;
	public String dialogueName;
	public boolean createDialogue;
	private boolean needToCreateDialogue;

	public String[] dialogueLines;
	
	public EventTrigger(String name, String contents) {
		super(name, contents);
		
		createDialogue = false;
		needToCreateDialogue = false;
		
		Matcher m = dialoguePattern.matcher(contents);
		while (m.find()) {
			if (m.group(1) != null) {
				dialogueName = m.group(2);
			}
			
			if (m.group(3) != null) {
				createDialogue = true;
				needToCreateDialogue = true;
				
				dialogue = m.group(4);
				dialogueName = name + "_Dialogue_" +dialogue + "_01";

				// TODO: Stupid 100 size arrays
				dialogueLines = new String[100];
				int max = -1;
				
				Matcher creationMatcher = dialogueCreationPattern.matcher(m.group(5));
				
				while (creationMatcher.find()) {
					int val = Integer.parseInt(creationMatcher.group(1));
					dialogueLines[val] = creationMatcher.group(2);
					max = Math.max(max, val);
				}
				 
				dialogueLines = Arrays.copyOf(dialogueLines, max + 1);
			}
		}
	}

	public void execute() {
		if (needToCreateDialogue) {
			for (int i = 0; i < dialogueLines.length; ++i) {
				String next = i + 1 == dialogueLines.length ? "" : "next[0]: " + name + "_Dialogue_" + dialogue + String.format("_%02d", i + 2);
				Game.getData().addDialogue(name + "_Dialogue_" + dialogue + String.format("_%02d", i + 1), "text: \"" + dialogueLines[i] + "\"\n" + next);
			}

			needToCreateDialogue = false;
		}

		super.execute();
		if (dialogueName != null) {
			Game.setMapViewDialogue(this.dialogueName);
		}
	}
	
	public String toString() {
		return "EventTrigger: " + name + " dialogue:" + dialogueName + " global:" + globals.toString();
	}
	
	public String triggerDataAsString() {
		StringBuilder ret = new StringBuilder(super.triggerDataAsString());
		
		if (createDialogue) {
			ret.append("\tcreateDialogue: ")
					.append(dialogue)
					.append("\n");
			
			for (int currDialogue = 0; currDialogue < dialogueLines.length; ++currDialogue) {
				ret.append("\ttext[")
						.append(currDialogue)
						.append("]: \"")
						.append(dialogueLines[currDialogue].trim())
						.append("\"\n");
			}
		}
		else {
			ret.append("\tdialogue: ")
					.append(dialogueName)
					.append("\n");
		}
		
		return ret.toString();
	}
}
