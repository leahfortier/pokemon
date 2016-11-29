package map;

import main.Game;
import trainer.CharacterData;
import util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Condition {
	private static final Pattern functionPattern = Pattern.compile("([\\w$]+)|([()&|!])");
	
	/*
	 * postfixed boolean function
	 */
	private List<String> condition;
	private String originalConditionString;
	
	public Condition(String conditionString) {

		originalConditionString = StringUtils.empty();
		condition = new ArrayList<>();

		if (StringUtils.isNullOrEmpty(conditionString)) {
			condition.add("true");
		} else {
			Matcher m = functionPattern.matcher(conditionString);
			Stack<String> stack = new Stack<>();
			
			while (m.find()) {
				String s = m.group();
				originalConditionString += s;

				switch (s) {
					case "(":
						stack.push(s);
						break;
					case ")":
						while (!stack.peek().equals("("))
							condition.add(stack.pop());

						stack.pop();
						break;
					case "&":
						while (!stack.isEmpty() && stack.peek().equals("!"))
							condition.add(stack.pop());

						stack.push(s);
						break;
					case "|":
						while (!stack.isEmpty() && (stack.peek().equals("&") || stack.peek().equals("!")))
							condition.add(stack.pop());

						stack.push(s);
						break;
					case "!":
						stack.push(s);
						break;
					default:
						condition.add(s);
						break;
				}
			}
			
			while (!stack.isEmpty()) {
				condition.add(stack.pop());
			}
		}
	}
	
	public boolean isTrue() {
		CharacterData player = Game.getPlayer();

		Stack<Boolean> stack = new Stack<>();
		for (String s: condition) {
			switch (s) {
				case "&": {
					boolean v1 = stack.pop();
					boolean v2 = stack.pop();

					stack.push(v1 && v2);
					break;
				}
				case "|": {
					boolean v1 = stack.pop();
					boolean v2 = stack.pop();

					stack.push(v1 || v2);
					break;
				}
				case "!":
					stack.push(!stack.pop());
					break;
				case "true":
					stack.push(true);
					break;
				case "false":
					stack.push(false);
					break;
				default:
					if (s.contains("$")) {
						int index = s.indexOf('$');
						String npcEntityName = s.substring(0, index);
						String interactionName = s.substring(index + 1);
						stack.push(player.isNpcInteraction(npcEntityName, interactionName));
					} else {
						stack.push(player.globalsContain(s));
					}
					break;
			}
		}
		
		return stack.pop();
	}

	public static String and(final String firstCondition, final String secondCondition) {
		if (StringUtils.isNullOrEmpty(firstCondition)) {
			if (StringUtils.isNullOrEmpty(secondCondition)) {
				return StringUtils.empty();
			}

			return secondCondition;
		} else if (StringUtils.isNullOrEmpty(secondCondition)) {
			return firstCondition;
		} else {
			return String.format("(%s)&(%s)", firstCondition, secondCondition);
		}

	}
	
	public boolean add(String global, char op) {
		if (op != '&' && op != '|') {
			return false;
		}
		
		originalConditionString = (originalConditionString.isEmpty()
				? StringUtils.empty()
				: ("(" + originalConditionString + ")" + op));
		originalConditionString += global;
		
		boolean negate = false;
		if (global.charAt(0) == '!') {
			global = global.substring(1, global.length());
			negate = true;
		}
		
		boolean noOp = false;
		if (condition.size() == 1 && condition.get(0).equals("true")) {
			condition.clear();
			noOp = true;
		}
		
		condition.add(global);
		if (negate) {
			condition.add("!");
		}
		
		if (!noOp) {
			condition.add(op + "");
		}
		
		return true;
	}
	
	public String toString() {
		return condition.toString();
	}
}
