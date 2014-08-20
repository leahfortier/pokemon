package map;

import trainer.CharacterData;

import java.util.ArrayList;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Condition {
	private static final Pattern conditionPattern = Pattern.compile("condition:\\s*(\\S+)");
	private static final Pattern functionPattern = Pattern.compile("(\\w+)|([()&|!])");
	/*
	 * postfixed boolean function
	 */
	private ArrayList<String> condition;
	private String originalConditionString;
	
	public Condition(String str){
		
		originalConditionString = "";
		condition = new ArrayList<>();
		Matcher conditionMatcher = conditionPattern.matcher(str);
		
		if (conditionMatcher.find()){
			
			String function = conditionMatcher.group(1);
			Matcher m = functionPattern.matcher(function);
			Stack<String> stack = new Stack<>();
			
			while (m.find()){
				String s = m.group();
				originalConditionString += s;
				if (s.equals("(")){
					stack.push(s);
				}else if (s.equals(")")){
					while (!stack.peek().equals("("))
						condition.add(stack.pop());
					stack.pop();
				}else if (s.equals("&")){
					while (!stack.isEmpty() && stack.peek().equals("!"))
						condition.add(stack.pop());
					stack.push(s);
				}else if (s.equals("|")){
					while (!stack.isEmpty() && (stack.peek().equals("&") || stack.peek().equals("!")))
						condition.add(stack.pop());
					stack.push(s);
				}else if (s.equals("!")){
					stack.push(s);
				}else{
					condition.add(s);
				}
			}
			while (!stack.isEmpty())
				condition.add(stack.pop());
		}else condition.add("true");
	}
	
	public boolean isTrue(CharacterData data){
		Stack<Boolean> stack = new Stack<>();
		for (String s: condition){
			if (s.equals("&")){
				boolean v1 = stack.pop();
				boolean v2 = stack.pop();
				stack.push(v1&&v2);
			}else if (s.equals("|")){
				boolean v1 = stack.pop();
				boolean v2 = stack.pop();
				stack.push(v1||v2);
			}else if (s.equals("!")){
				stack.push(!stack.pop());
			}else if (s.equals("true")){
				stack.push(true);
			}else if (s.equals("false")){
				stack.push(false);
			}else{
				stack.push(data.globalsContain(s));
			}
		}
		return stack.pop();
	}
	
	public boolean add(String global, char op)
	{
		if (op != '&' && op != '|')
			return false;
		
		originalConditionString = (originalConditionString.length() == 0? "":"("+originalConditionString+")" +op) +global;
		
		boolean negate = false;
		if (global.charAt(0) == '!')
		{
			global = global.substring(1,global.length());
			negate = true;
		}
		
		boolean noOp = false;
		if (condition.size() == 1 && condition.get(0).equals("true"))
		{
			condition.clear();
			noOp = true;
		}
		
		condition.add(global);
		if (negate)
			condition.add("!");
		if (!noOp)
			condition.add(""+op);
		return true;
	}
	
	public String toString()
	{
		return condition.toString();
	}
	
	public String getOriginalConditionString() {
		return originalConditionString;
	}
}
