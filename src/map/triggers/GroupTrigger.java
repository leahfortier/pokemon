package map.triggers;

import java.util.ArrayList;
import java.util.regex.Matcher;

import main.Game;

public class GroupTrigger extends Trigger{
	
	public ArrayList<String> triggers;
	
	public GroupTrigger(String name, String contents) {
		super(name, contents);
		triggers = new ArrayList<>();
		Matcher m = variablePattern.matcher(contents);
		while (m.find()){
			if (m.group(1).equals("trigger"))
				triggers.add(m.group(2));
		}
	}
	@Override
	public void execute(Game game){
		super.execute(game);
		for (String s: triggers){
			Trigger trig = game.data.getTrigger(s);
			if (trig != null && trig.isTriggered(game.charData)){
				trig.execute(game);
			}
		}
	}
	
	@Override
	public String toString() {
		return "GroupTrigger: " + name + " triggers: " + triggers.toString();
	}
	
	@Override
	public String triggerDataAsString() {
		StringBuilder ret = new StringBuilder(super.triggerDataAsString());
		
		for (String trigger: triggers) {
			ret.append("\ttrigger: " +trigger +"\n");
		}
		
		return ret.toString();
	}
}
