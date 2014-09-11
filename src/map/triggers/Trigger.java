package map.triggers;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.Game;
import map.Condition;
import trainer.CharacterData;

public abstract class Trigger 
{
	private static final Pattern globalPattern = Pattern.compile("global:\\s*([!]?\\w+)");
	protected static final Pattern variablePattern = Pattern.compile("(\\w+):\\s*([\\w -.']+)", Pattern.UNICODE_CHARACTER_CLASS);
	
	protected String name;
	protected ArrayList<String> globals;
	protected Condition condition;
	
	protected Trigger(String name, String str)
	{
		this.name = name;
		
		condition = new Condition(str);
		
		globals = new ArrayList<String>();
		Matcher m = globalPattern.matcher(str);
	
		while (m.find())
		{
			globals.add(m.group(1));
		}		
	}
	
	/**
	 * Evaluate the function, Should only be triggered when a player moves into a map squared that is
	 * defined to trigger this event
	 * @param data
	 * @return
	 */
	public boolean isTriggered(CharacterData data)
	{
		return condition.isTrue(data);
	}
	
	public void execute(Game game) 
	{
		for (String s: globals)
		{
			if (s.charAt(0) == '!')
				game.charData.removeGlobal(s.substring(1));
			else game.charData.addGlobal(s);
		}
	}
	
	public String getName()
	{
		return name;
	}
	
	public String triggerDataAsString() 
	{
		StringBuilder ret = new StringBuilder();
		
		if (!condition.getOriginalConditionString().equals("")) 
		{
			ret.append("\tcondition: " + condition.getOriginalConditionString() + "\n");
		}
		
		for (String global: globals) 
		{
			ret.append("\tglobal: " + global + "\n");
		}
		
		return ret.toString();
	}
	
	public Condition getCondition() 
	{
		return condition;
	}
	
	public ArrayList<String> getGlobals() 
	{
		return globals;
	}
}
