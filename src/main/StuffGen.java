package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import battle.Attack;

public class StuffGen 
{
	private static String POKEMON_EFFECT_PATH = "src" + Global.FILE_SLASH + "battle" + Global.FILE_SLASH + "effect" + Global.FILE_SLASH + "PokemonEffect.java";
	private static String TEAM_EFFECT_PATH = "src" + Global.FILE_SLASH + "battle" + Global.FILE_SLASH + "effect" + Global.FILE_SLASH + "TeamEffect.java";
	private static String BATTLE_EFFECT_PATH = "src" + Global.FILE_SLASH + "battle" + Global.FILE_SLASH + "effect" + Global.FILE_SLASH + "BattleEffect.java";
	private static String MOVE_PATH = "src" + Global.FILE_SLASH + "battle" + Global.FILE_SLASH + "Attack.java";
	private static String ABILITY_PATH = "src" + Global.FILE_SLASH + "pokemon" + Global.FILE_SLASH + "Ability.java";
	
	private static String ITEM_PATH = "src" + Global.FILE_SLASH + "item" + Global.FILE_SLASH + "Item.java", 
			ITEM_TILES_PATH = "rec" + Global.FILE_SLASH + "tiles" + Global.FILE_SLASH + "itemTiles" + Global.FILE_SLASH;
	
	private static HashMap<String, String> moveFields = new HashMap<String, String>();
	private static HashMap<String, String> effectClasses = new HashMap<String, String>();
	static
	{
		moveFields.put("Pow", "power");
		moveFields.put("Power", "power");
		moveFields.put("Acc", "accuracy");
		moveFields.put("Accuracy", "accuracy");
		moveFields.put("Name", "name");
		moveFields.put("Desc", "desc");
		moveFields.put("Description", "desc");
		moveFields.put("PP", "pp");
		moveFields.put("Type", "type");
		moveFields.put("Status", "status");
		moveFields.put("Cat", "category");
		moveFields.put("Category", "category");
		moveFields.put("Eff", "effects");
		moveFields.put("Effect", "effects");
		moveFields.put("Status", "status");
		moveFields.put("MoveType", "moveTypes");
		moveFields.put("EffChance", "effectChance");
		moveFields.put("EffectChance", "effectChance");
		moveFields.put("StatChange", "statChanges");
		moveFields.put("SelfTarget", "selfTarget");
		moveFields.put("PrintCast", "printCast");
		moveFields.put("Priority", "priority");
		effectClasses.put("EndTurn", "EndTurnEffect");
		effectClasses.put("AttackSelection", "AttackSelectionEffect");
		effectClasses.put("BeforeTurn", "BeforeTurnEffect");
		effectClasses.put("OpposingBeforeTurn", "OpposingBeforeTurnEffect");
		effectClasses.put("StatChanging", "StatChangingEffect");
		effectClasses.put("StageChanging", "StageChangingEffect");
		effectClasses.put("MoveCondition", "MoveCondition");
		effectClasses.put("MoveListCondition", "MoveListCondition");
		effectClasses.put("Entry", "EntryEffect");
		effectClasses.put("TypeCondition", "TypeCondition");
		effectClasses.put("AbilityCondition", "AbilityCondition");
		effectClasses.put("ItemCondition", "ItemCondition");
		effectClasses.put("Trapping", "TrappingEffect");
		effectClasses.put("OpponentTrapping", "OpponentTrappingEffect");
		effectClasses.put("PowerChange", "PowerChangeEffect");
		effectClasses.put("OpponentPowerChange", "OpponentPowerChangeEffect");
		effectClasses.put("StatSwitch", "StatSwitchingEffect");
		effectClasses.put("Levitation", "LevitationEffect");
		effectClasses.put("Grounded", "GroundedEffect");
		effectClasses.put("ForceMove", "ForceMoveEffect");
		effectClasses.put("StatsCondition", "StatsCondition");
		effectClasses.put("Integer", "IntegerCondition");
		effectClasses.put("EffectBlocker", "EffectBlockerEffect");
		effectClasses.put("WeatherBlocker", "WeatherBlockerEffect");
		effectClasses.put("StatProtector", "StatProtectingEffect");
		effectClasses.put("PhysicalContact", "PhysicalContactEffect");
		effectClasses.put("DamageBlocker", "DamageBlocker");
		effectClasses.put("Faint", "FaintEffect");
		effectClasses.put("ApplyDamageEffect", "ApplyDamageEffect");
		effectClasses.put("TakeDamage", "TakeDamageEffect");
		effectClasses.put("Passable", "PassableEffect");
		effectClasses.put("StatusPrevention", "StatusPreventionEffect");
		effectClasses.put("ChangeAbility", "ChangeAbilityMove");
		effectClasses.put("ChangeType", "ChangeTypeMove");
		effectClasses.put("SwitchOut", "SwitchOutEffect");
		effectClasses.put("ModifyStageValue", "ModifyStageValueEffect");
		effectClasses.put("Bracing", "BracingEffect");
		effectClasses.put("EndBattle", "EndBattleEffect");
		effectClasses.put("Stalling", "StallingEffect");
		effectClasses.put("DefiniteEscape", "DefiniteEscape");
		effectClasses.put("CritStage", "CritStageEffect");
		effectClasses.put("CritBlocker", "CritBlockerEffect");
	}
	
	public StuffGen()
	{
		attackGen();
		pokemonEffectGen();
		teamEffectGen();
		battleEffectGen();
		abilityGen();
		itemGen();
	}
	
	private static void effectGen(String input, String output, String superClass)
	{
		Scanner in = openFile(input), effects = openFile(output);
		StringBuilder out = new StringBuilder(), classes = new StringBuilder();
		while (effects.hasNext())
		{
			String line = effects.nextLine();
			out.append(line + "\n");
			if (line.contains("// EVERYTHING BELOW IS GENERATED ###")) break;
		}
		effects.close();
		
		out.append("\n\t\t// List all of the effects we are loading\n");
		while (in.hasNext())
		{
			String line = in.nextLine().trim();
			while (in.hasNext() && (line.equals("") || line.charAt(line.length() - 1) != ':'))
			{
				line = in.nextLine().trim();
			}
			
			String name = line.substring(0, line.length() - 1);
			
			boolean canHave = false, first = true, nextTurnSubside = false, implemented = false;
			String interfaces = "", castMsg = "", applyMsg = "", subsideMsg = "", apply = "", rapidSpin = "", 
					failure = "", minTurns = "-1", maxTurns = "-1", cast = "", canAttack = "", statChanging = "",
					extraFields = "", activate = "", subside = "", getMove = "", shouldSubside = "", stageChange = "",
					usable = "", unusableMsg = "", enter = "", getType = "", getTurns = "", getMultiplier = "",
					statSwitch = "", getPokemon = "", effectBlocker = "", integer = "", failMessage = "", decTurns = "",
					defog = "", prevent = "", preventMessage = "", opposingCanAttack = "", getAbility = "",
					deathwish = "", statusPrevent = "", statusPreventMessage = "", brace = "", braceMessage = "",
					getItem = "", getMoveList = "", getStat = "", endBattle = "", increaseCrits = "", critBlocker = "";
			
			line = in.nextLine().trim();
			while (in.hasNextLine() && !line.equals("*"))
			{
				String[] split = line.split(":", 2);
				if (split.length > 1) split[1] = split[1].trim();
				
				switch (split[0])
				{
					case "EffectType":
						if (!effectClasses.containsKey(split[1])) Global.error("Undefined Pokemon Effect Type " + split[1] + ". (Effect: " + name + ")");
						interfaces += (implemented ? ", " : "implements ") + effectClasses.get(split[1]);
						implemented = true;
						break;
					case "CanHave":
						canHave = true;
						break;
					case "NextTurnSubside":
						nextTurnSubside = true;
						break;
					case "ShouldSubside":
						shouldSubside = readFunction(in, line, split[1]);
						shouldSubside = writeFunction("boolean shouldSubside(Battle b, ActivePokemon victim)", shouldSubside);
						break;
					case "Enter":
						enter = readFunction(in, line, split[1]);
						enter = writeFunction("void enter(Battle b, ActivePokemon victim)", enter);
						break;
					case "Prevent":
						prevent = readFunction(in, line, split[1]);
						prevent = writeFunction("boolean prevent(ActivePokemon caster, Stat stat)", prevent);
						break;
					case "PreventMessage":
						preventMessage = writeFunction("String preventionMessage(ActivePokemon p)", "\t\t\treturn " + split[1] + ";\n");
						break;
					case "SwitchStat":
						statSwitch = readFunction(in, line, split[1]);
						statSwitch = writeFunction("Stat switchStat(Stat s)", statSwitch);
						break;
					case "GetMultiplier":
						getMultiplier = readFunction(in, line, split[1]);
						getMultiplier = writeFunction("double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)", getMultiplier);
						break;
					case "GetTurns":
						getTurns = readFunction(in, line, split[1]);
						getTurns = writeFunction("int getTurns()", getTurns);
						break;
					case "DecTurns":
						decTurns = readFunction(in, line, split[1]);
						decTurns = writeFunction("void decrementTurns()", decTurns);
						break;
					case "GetPokemon":
						getPokemon = readFunction(in, line, split[1]);
						getPokemon = writeFunction("ActivePokemon getPokemon()", getPokemon);
						break;
					case "StageChange":
						stageChange = readFunction(in, line, split[1]);
						stageChange = writeFunction("int adjustStage(int stage, Stat s, ActivePokemon p, ActivePokemon opp, Battle b, boolean user)", stageChange);
						break;
					case "FailType":
						for (String type : split[1].split(" ")) 
						{
							failure += (first ? "" : " || ") + "victim.isType(b, Type." + type.toUpperCase() + ")";
							first = false;
						}
						break;
					case "FailAbility":
						failure += (first ? "" : " || ") + "(victim.hasAbility(\"" + split[1] + "\") && !caster.breaksTheMold())";
						first = false;
						break;
					case "FailCondition":
						failure += (first ? "" : " || ") + split[1];
						first = false;
						break;
					case "FailMessage":
						failMessage = readFunction(in, line, split[1]);
						failMessage += "\t\t\treturn super.getFailMessage(b, user, victim, team);\n";
						failMessage = writeFunction("String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim, boolean team)", failMessage);
						break;
					case "Cast":
						cast = readFunction(in, line, split[1]);
						cast = writeFunction("void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)", cast);
						break;
					case "CastMessage":
						castMsg = writeFunction("String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)", "\t\t\treturn " + split[1] + ";\n");
						break;
					case "ApplyMessage":
						applyMsg = writeFunction("String getApplyMessage(ActivePokemon user, ActivePokemon victim)", "\t\t\treturn " + split[1] + ";\n");
						break;
					case "SubsideMessage":
						subsideMsg = writeFunction("String getSubsideMessage(ActivePokemon victim)", "\t\t\treturn " + split[1] + ";\n");
						break;
					case "NumTurns":
						minTurns = split[1];
						maxTurns = split[1];
						break;
					case "MinTurns":
						minTurns = split[1];
						break;
					case "MaxTurns":
						maxTurns = split[1];
						break;
					case "Apply":
						apply = readFunction(in, line, split[1]);
						apply = writeFunction("void apply(ActivePokemon victim, Battle b)", apply);
						break;
					case "PartialTrap":
						apply = "\t\t\tif (victim.hasAbility(\"Magic Guard\")) return;\n";
						apply += "\t\t\tb.addMessage(" + split[1] + ");\n";
						apply += "\t\t\tvictim.reduceHealthFraction(b, b.getOtherPokemon(victim.user()).isHoldingItem(b, \"Binding Band\") ? 1/8.0 : 1/16.0);\n";
						apply = writeFunction("void apply(ActivePokemon victim, Battle b)", apply);
						cast = "\t\t\tsuper.cast(b, caster, victim, source, printCast);\n";
						cast += "\t\t\tif (caster.isHoldingItem(b, \"Grip Claw\")) setTurns(5);\n";
						cast = writeFunction("void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)", cast);
						break;
					case "CanAttack":
						canAttack = readFunction(in, line, split[1]);
						canAttack = writeFunction("boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b)", canAttack);
						break;
					case "OpposingCanAttack":
						opposingCanAttack = readFunction(in, line, split[1]);
						opposingCanAttack = writeFunction("boolean opposingCanAttack(ActivePokemon p, ActivePokemon opp, Battle b)", opposingCanAttack);
						break;
					case "RapidSpin":
						interfaces += (implemented ? ", " : "implements ") + "RapidSpinRelease";
						implemented = true;
						rapidSpin = writeFunction("String getReleaseMessage(ActivePokemon user)", "\t\t\treturn " + split[1] + ";\n");						
						break;
					case "Defog":
						interfaces += (implemented ? ", " : "implements ") + "DefogRelease";
						implemented = true;
						defog = writeFunction("String getDefogReleaseMessage(ActivePokemon victim)", "\t\t\treturn " + split[1] + ";\n");
						break;
					case "IncreaseCritStage":
						increaseCrits = readFunction(in, line, split[1]);
						increaseCrits = writeFunction("int increaseCritStage(ActivePokemon p)", increaseCrits);
						break;
					case "CritBlocker":
						critBlocker = readFunction(in, line, split[1]);
						critBlocker = writeFunction("boolean blockCrits()", critBlocker);
						break;
					case "EndBattle":
						endBattle = readFunction(in, line, split[1]);
						endBattle = writeFunction("void afterBattle(Trainer player, Battle b, ActivePokemon p)", endBattle);
						break;
					case "Bracing":
						brace = readFunction(in, line, split[1]);
						brace = writeFunction("boolean isBracing(Battle b, ActivePokemon bracer, Boolean fullHealth)", brace);
						break;
					case "BraceMessage":
						braceMessage = writeFunction("String braceMessage(ActivePokemon bracer)", "\t\t\treturn " + split[1] + ";\n");
						break;
					case "Modify":
						statChanging = readFunction(in, line, split[1]);
						statChanging = writeFunction("int modify(int stat, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)", statChanging);
						break;
					case "StatusPrevent":
						statusPrevent = readFunction(in, line, split[1]);
						statusPrevent = writeFunction("boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status)", statusPrevent);
						break;
					case "StatusPreventMessage":
						statusPreventMessage = writeFunction("String preventionMessage(ActivePokemon victim)", "\t\t\treturn " + split[1] + ";\n");
						break;
					case "Field":
						extraFields += readFunction(in, line, split[1], 2);
						break;
					case "Activate":
						activate = readFunction(in, line, split[1]);
						activate = writeFunction(name + " newInstance()", "\t\t\t" + name + " x = (" + name + ")(new " + name + "().activate());\n" + activate + "\t\t\treturn x;\n");
						break;
					case "Subside":
						subside = readFunction(in, line, split[1]);
						subside = writeFunction("void subside(Battle b, ActivePokemon p)", subside);
						break;
					case "GetMove":
						getMove = readFunction(in, line, split[1]);
						getMove = writeFunction("Move getMove()", getMove);
						break;
					case "GetMoveList":
						getMoveList = readFunction(in, line, split[1]);
						getMoveList = writeFunction("List<Move> getMoveList(ActivePokemon p, List<Move> moves)", getMoveList);
						break;
					case "GetType":
						getType = readFunction(in, line, split[1]);
						getType = writeFunction("Type[] getType()", getType);
						break;
					case "GetAbility":
						getAbility = readFunction(in, line, split[1]);
						getAbility = writeFunction("Ability getAbility()", getAbility);
						break;
					case "GetItem":
						getItem = readFunction(in, line, split[1]);
						getItem = writeFunction("Item getItem()", getItem);
						break;
					case "GetStat":
						getStat = readFunction(in, line, split[1]);
						getStat = writeFunction("int getStat(Stat stat)", getStat);
						break;
					case "Deathwish":
						deathwish = readFunction(in, line, split[1]);
						deathwish = writeFunction("void deathwish(Battle b, ActivePokemon dead, ActivePokemon murderer)", deathwish);
						break;
					case "Integer":
						integer = writeFunction("int getAmount()", "\t\t\treturn " + split[1] + ";\n");
						integer += writeFunction("void decrease(int amount)", "\t\t\t" + split[1] + " -= amount;\n");
						integer += writeFunction("void increase(int amount)", "\t\t\t" + split[1] + " += amount;\n");
						break;
					case "ValidMove":
						effectBlocker = readFunction(in, line, split[1]);
						effectBlocker = writeFunction("boolean validMove(Battle b, ActivePokemon user, ActivePokemon victim)", effectBlocker);
						break;
					case "Usable":
						usable = readFunction(in, line, split[1]);
						usable = writeFunction("boolean usable(ActivePokemon p, Move m)", usable);
						break;
					case "UnusableMessage":
						unusableMsg = writeFunction("String getUnusableMessage(ActivePokemon p)", "\t\t\treturn " + split[1] + ";\n");
						break;
					default:
						Global.error(split[0] + " is not a valid pokemon effect field declaration (Effect: " + name + ")"); // SUCKS TO SUCK
				}
				
				line = in.nextLine().trim();
			}
			classes.append("\tprivate static class " + name + " extends " + superClass + " " + interfaces + "\n\t{\n");
			classes.append("\t\tprivate static final long serialVersionUID = 1L;\n");
			classes.append(extraFields + (extraFields.length() > 0 ? "\n" : ""));
			classes.append("\t\tpublic " + name + "()\n\t\t{\n");
			classes.append("\t\t\tsuper(\"" + name + "\", " + minTurns + ", " + maxTurns + ", " + nextTurnSubside + ");\n");
			classes.append("\t\t}\n");
			
			// Add newInstance() method
			if (activate.length() > 0) classes.append(activate);
			else classes.append(writeFunction(name + " newInstance()", "\t\t\treturn (" + name + ")(new " + name + "().activate());\n"));
			
			// Create applies method
			if (!canHave) 
			{
				failure += first ? "" : " || ";
				switch (superClass)
				{ 
					case "PokemonEffect":
						failure += "victim.hasEffect(\"" + name + "\")";
						break;
					case "TeamEffect":
						failure += "Effect.hasEffect(b.getEffects(victim.user()), \"" + name + "\")";
						break;
					case "BattleEffect":
						failure += "Effect.hasEffect(b.getEffects(), \"" + name + "\")";
						break;
					default:
						Global.error("YO WHAT THE FUCK INCORRECT SUPERCLASS");
						break;
				}	
			}
			
			if (failure.length() > 0)
			{
				classes.append(writeFunction("boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)", "\t\t\treturn !(" + failure + ");\n"));
			}

			classes.append(apply);
			classes.append(canAttack);
			classes.append(opposingCanAttack);
			classes.append(cast);
			classes.append(castMsg);
			classes.append(applyMsg);
			classes.append(subsideMsg);
			classes.append(rapidSpin);
			classes.append(statChanging);
			classes.append(getMove);
			classes.append(subside);
			classes.append(usable);
			classes.append(unusableMsg);
			classes.append(shouldSubside);
			classes.append(enter);
			classes.append(getType);
			classes.append(decTurns);
			classes.append(getTurns);
			classes.append(stageChange);
			classes.append(statusPrevent);
			classes.append(statusPreventMessage);
			classes.append(getMultiplier);
			classes.append(statSwitch);
			classes.append(getPokemon);
			classes.append(integer);
			classes.append(effectBlocker);
			classes.append(prevent);
			classes.append(preventMessage);
			classes.append(deathwish);
			classes.append(failMessage);
			classes.append(defog);
			classes.append(getAbility);
			classes.append(getItem);
			classes.append(brace);
			classes.append(braceMessage);
			classes.append(getMoveList);
			classes.append(getStat);
			classes.append(endBattle);
			classes.append(increaseCrits);
			classes.append(critBlocker);
			
			classes.append("\t}\n\n");
			out.append("\t\tmap.put(\"" + name + "\", new " + name + "());\n");
		}
		
		out.append("\t}\n\n");
		out.append("\t/**** WARNING DO NOT PUT ANY VALUABLE CODE HERE IT WILL BE DELETED *****/\n\n"); // DON'T DO IT
		out.append(classes + "}");
		
		printToFile(output, out);
	}
	
	private static void teamEffectGen()
	{
		effectGen("TeamEffects.txt", TEAM_EFFECT_PATH, "TeamEffect");
	}
	
	private static void battleEffectGen()
	{
		effectGen("BattleEffects.txt", BATTLE_EFFECT_PATH, "BattleEffect");
	}
	
	private static void pokemonEffectGen()
	{
		effectGen("PokemonEffects.txt", POKEMON_EFFECT_PATH, "PokemonEffect");
	}
	
	private static void attackGen()
	{
		Scanner in = openFile("Moves.txt"), moves = openFile(MOVE_PATH);
		StringBuilder out = new StringBuilder(), classes = new StringBuilder(), temp = new StringBuilder();
		while (moves.hasNext())
		{
			String line = moves.nextLine();
			out.append(line + "\n");
			if (line.contains("// EVERYTHING BELOW IS GENERATED ###")) break;
		}
		moves.close();
		temp.append(out);
		
		out.append("\n\t\t// List all of the moves we are loading\n");
		while (in.hasNext())
		{
			String line = in.nextLine().trim();
			while (in.hasNext() && (line.equals("") || line.charAt(line.length() - 1) != ':'))
			{
				line = in.nextLine().trim();
			}
			
			String className = line.substring(0, line.length() - 1);
			
			line = in.nextLine().trim();
			String applyEffects = "", recoil = "", selfHealing = "", interfaces = "", fields = "", getPower = "",
				name = className, desc = "", pp = "", category = "", type = "", multiTurn = "", isMultiTurn = "",
				charge = "", applyDamage = "", apply = "", changeType = "", getAcc = "", stageChange = "",
				extraFields = "", getType = "", statSwitch = "", crashDamage = "", startTurn = "",
				physicalContact = "", changeAbility = "", abilityMessage = "", getPriority = "", getItem = "";
			boolean implemented = false, power = false, accuracy = false, selfTarget = false, fieldMove = false, 
					bouncy = false;
			while (in.hasNext() && !line.equals("*"))
			{
				String[] split = line.split(":", 2), mcsplit = split[1].trim().split(" ");
				split[1] = split[1].trim();
				
				String field = moveFields.get(split[0]);
				if (field == null)
				{
					switch (split[0])
					{
						case "ApplyEffects":
							applyEffects = readFunction(in, line, split[1]);
							applyEffects = writeFunction("void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)", applyEffects);
							break;
						case "Apply":
							apply = readFunction(in, line, split[1]);
							apply = writeFunction("void apply(ActivePokemon me, ActivePokemon o, Battle b)", apply);
							break;
						case "ApplyDamage":
							applyDamage = readFunction(in, line, split[1]);
							applyDamage = writeFunction("int applyDamage(ActivePokemon me, ActivePokemon o, Battle b)", applyDamage);
							break;
						case "FixedDamage":
							applyDamage = "\t\t\tif (super.zeroAdvantage(b, me, o)) return -1;\n";
							applyDamage += "\t\t\treturn b.applyDamage(o, " + split[1] + ");\n";
							applyDamage = writeFunction("int applyDamage(ActivePokemon me, ActivePokemon o, Battle b)", applyDamage);
							break;
						case "SwitchStat":
							interfaces += (implemented ? ", " : "implements ") + "StatSwitchingEffect";
							implemented = true;
							statSwitch = readFunction(in, line, split[1]);
							statSwitch = writeFunction("Stat switchStat(Stat s)", statSwitch);
							break;
						case "StageChange":
							interfaces += (implemented ? ", " : "implements ") + "StageChangingEffect";
							implemented = true;
							stageChange = readFunction(in, line, split[1]);
							stageChange = writeFunction("int adjustStage(int stage, Stat s, ActivePokemon p, ActivePokemon opp, Battle b, boolean user)", stageChange);
							break;
						case "StartTurn":
							startTurn = readFunction(in, line, split[1]);
							startTurn = writeFunction("void startTurn(Battle b, ActivePokemon me)", startTurn);
							break;
						case "GetPriority":
							getPriority = readFunction(in, line, split[1]);
							getPriority = writeFunction("int getPriority(Battle b, ActivePokemon me)", getPriority);
							break;
						case "Field":
							extraFields += readFunction(in, line, split[1], 2);
							break;
						case "MultiStrike":
							applyDamage = "\t\t\tif (super.zeroAdvantage(b, me, o)) return -1;\n";
							applyDamage += "\t\t\tint damage = 0, hits = (int)(Math.random()*(" + mcsplit[1] + " - " + mcsplit[0] + " + 1)) + " + mcsplit[0] + ";\n";
							if (mcsplit[1].equals("5")) applyDamage += "\t\t\tif (me.hasAbility(\"Skill Link\")) hits = 5;\n";
							applyDamage += "\t\t\tfor (int i = 1; i <= hits; i++)\n\t\t\t{\n";
							applyDamage += "\t\t\t\tb.addMessage(\"Hit \" + i + \"!\");\n";
							applyDamage += "\t\t\t\tdamage += super.applyDamage(me, o, b);\n\t\t\t}\n";
							applyDamage += "\t\t\tb.addMessage(\"Hit \" + hits + \" times!\");\n";
							applyDamage += "\t\t\treturn damage;\n";
							applyDamage = writeFunction("int applyDamage(ActivePokemon me, ActivePokemon o, Battle b)", applyDamage);
							break;
						case "Recoil":
							interfaces += (implemented ? ", " : "implements ") + "RecoilMove";
							implemented = true;
							String recoilDamage = "";
							if (!className.equals("Struggle")) recoil = "\t\t\tif (user.hasAbility(\"Rock Head\") || user.hasAbility(\"Magic Guard\")) return;\n";
							recoil += "\t\t\tb.addMessage(user.getName() + \" was hurt by recoil!\");\n";
							recoil += "\t\t\tb.applyDamage(user, recoilDamage(user, damage));\n";
							try { recoilDamage = "\t\t\treturn (int)Math.ceil(damage/" + Integer.parseInt(split[1]) + ".0);\n"; }
							catch(NumberFormatException ex) { recoilDamage = readFunction(in, line, split[1]); }
							extraFields += "\t\tprivate int recoilDamage(ActivePokemon user, int damage)\n\t\t{\n" + recoilDamage + "\t\t}\n";
							recoil = writeFunction("void applyRecoil(Battle b, ActivePokemon user, Integer damage)", recoil);
							break;
						case "CrashDamage":
							interfaces += (implemented ? ", " : "implements ") + "CrashDamageMove";
							implemented = true;
							crashDamage = "\t\t\tb.addMessage(user.getName() + \" kept going and crashed!\");\n";
							crashDamage += "\t\t\tb.applyDamage(user, user.getStat(Stat.HP)/" + split[1] + ");\n";
							crashDamage = writeFunction("void crash(Battle b, ActivePokemon user)", crashDamage);
							break;
						case "SelfHealing":
							fields += "\t\t\tsuper.selfTarget = true;\n";
							interfaces += (implemented ? ", " : "implements ") + "SelfHealingMove";
							implemented = true;
							selfTarget = true;
							selfHealing = "\t\t\tif (victim.fullHealth() || victim.hasEffect(\"HealBlock\"))\n\t\t\t{\n\t\t\t\tb.addMessage(\"...but it failed!\");\n\t\t\t\treturn;\n\t\t\t}\n\n";
							try { selfHealing += "\t\t\tvictim.healHealthFraction(1/" + Integer.parseInt(split[1]) + ".0);\n"; }
							catch(NumberFormatException ex) { selfHealing += readFunction(in, line, split[1]); }
							selfHealing += "\t\t\tb.addMessage(victim.getName() + \"'s health was restored!\", victim.getHP(), victim.user());\n";
							selfHealing = writeFunction("void heal(ActivePokemon user, ActivePokemon victim, Battle b)", selfHealing);
							break;
						case "ChangeType":
							interfaces += (implemented ? ", " : "implements ") + "ChangeTypeMove";
							implemented = true;
							changeType = readFunction(in, line, split[1]);
							changeType = writeFunction("Type[] getType(Battle b, ActivePokemon caster, ActivePokemon victim)", changeType);
							break;
						case "AbilityChange":
							interfaces += (implemented ? ", " : "implements ") + "ChangeAbilityMove";
							implemented = true;
							changeAbility = readFunction(in, line, split[1]);
							changeAbility = writeFunction("Ability getAbility(Battle b, ActivePokemon caster, ActivePokemon victim)", changeAbility);
							break;
						case "SwitchItems":
							interfaces += (implemented ? ", " : "implements ") + "ItemCondition";
							implemented = true;
							line = in.nextLine().trim();
							extraFields += "\t\tprivate Item item;\n";
							applyEffects = "\t\t\tif (" + split[1] + ")\n\t\t\t{\n";
							applyEffects += "\t\t\t\tif (super.category == Category.STATUS) b.addMessage(\"...but it failed!\");\n";
							applyEffects += "\t\t\t\treturn;\n\t\t\t}\n\n";
							applyEffects += "\t\t\tItem userItem = user.getHeldItem(b), victimItem = victim.getHeldItem(b);\n";
							applyEffects += "\t\t\tb.addMessage(" + line + ");\n\n";
							applyEffects += "\t\t\tif (b.isWildBattle())\n\t\t\t{\n";
							applyEffects += "\t\t\t\tuser.giveItem((HoldItem)victimItem);\n";
							applyEffects += "\t\t\t\tvictim.giveItem((HoldItem)userItem);\n";
							applyEffects += "\t\t\t\treturn;\n\t\t\t}\n\n";
							applyEffects += "\t\t\titem = userItem;\n";
							applyEffects += "\t\t\tsuper.applyEffects(b, user, victim);\n";
							applyEffects += "\t\t\titem = victimItem;\n";
							applyEffects += "\t\t\tsuper.applyEffects(b, user, user);\n";
							applyEffects = writeFunction("void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)", applyEffects);
							getItem = writeFunction("Item getItem()", "\t\t\treturn item;\n");
							fields += "\t\t\tsuper.effects.add(Effect.getEffect(\"ChangeItem\", EffectType.POKEMON));\n";
							break;
						case "GetItem":
							interfaces += (implemented ? ", " : "implements ") + "ItemCondition";
							implemented = true;
							getItem = readFunction(in, line, split[1]);
							getItem = writeFunction("Item getItem()", getItem);
							break;
						case "AbilityMessage":
							abilityMessage = readFunction(in, line, split[1]);
							abilityMessage = writeFunction("String getMessage(Battle b, ActivePokemon caster, ActivePokemon victim)", abilityMessage);
							break;
						case "ExitBattle":
							applyEffects = "\t\t\tTrainer trainer = b.isWildBattle() ? null : (Trainer)b.getTrainer(victim.user());\n";
							applyEffects += "\t\t\tif (trainer == null || !trainer.hasRemainingPokemon() || b.isFirstAttack() || victim.hasEffect(\"Ingrain\"))\n\t\t\t{\n";
							applyEffects += "\t\t\t\tif (super.category == Category.STATUS) b.addMessage(\"...but it failed!\");\n";
							applyEffects += "\t\t\t\treturn;\n\t\t\t}\n\n";
							applyEffects += "\t\t\tif (victim.hasAbility(\"Suction Cups\") && !user.breaksTheMold())\n\t\t\t{\n";
							applyEffects += "\t\t\t\tb.addMessage(victim.getName() + \"'s Suction Cups prevents it from switching!\");\n";
							applyEffects += "\t\t\t\treturn;\n\t\t\t}\n\n";
							applyEffects += "\t\t\tb.addMessage(" + split[1] + ");\n";
							applyEffects += "\t\t\ttrainer.switchToRandom();\n";
							applyEffects += "\t\t\tvictim = trainer.front();\n";
							applyEffects += "\t\t\tb.enterBattle(victim, \"...and \" + victim.getName() + \" was dragged out!\");\n";
							applyEffects = writeFunction("void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)", applyEffects);
							break;
						case "SelfSwitching":
							if (!split[1].equals("True")) Global.error("SelfSwitching must be true (Move " + name + ")");
							applyEffects = "\t\t\tTeam t = b.getTrainer(user.user());\n";
							applyEffects += "\t\t\tif (t instanceof WildPokemon)\n\t\t\t{\n";
							applyEffects += "\t\t\t\tb.addMessage(user.getName() + \" left the battle!\");\n";
							applyEffects += "\t\t\t\tb.addMessage(\" \", MessageUpdate.Update.EXIT_BATTLE);\n";
							applyEffects += "\t\t\t\treturn;\n\t\t\t}\n\n";
							applyEffects += "\t\t\tTrainer trainer = (Trainer)t;\n";
							applyEffects += "\t\t\tif (!trainer.hasRemainingPokemon()) return;\n\n";
							applyEffects += "\t\t\tb.addMessage(user.getName() + \" went back to \" + trainer.getName() + \"!\");\n";
							applyEffects += "\t\t\ttrainer.switchToRandom(); // TODO: Prompt a legit switch fo user\n";
							applyEffects += "\t\t\tuser = trainer.front();\n";
							applyEffects += "\t\t\tb.enterBattle(user, trainer.getName() + \" sent out \" + user.getName() + \"!\");\n";
							applyEffects = writeFunction("void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)", applyEffects);
							break;
						case "MultiTurn":
							interfaces += (implemented ? ", " : "implements ") + "MultiTurnMove";
							implemented = true;
							mcsplit = split[1].split(" ", 3);
							charge = "\t\t\tb.addMessage(" + mcsplit[2] + ");\n";
							multiTurn += writeFunction("boolean chargesFirst()", "\t\t\treturn " + (mcsplit[0].equals("Yes")) + ";\n");
							multiTurn += writeFunction("boolean semiInvulnerability()", "\t\t\treturn " + (mcsplit[1].equals("Yes")) + ";\n");
							break;
						case "Charge":
							if (charge.length() == 0) Global.error("Charge function must be written after MultiTurn (Effect " + name + ")");
							charge += readFunction(in, line, split[1]);
							break;
						case "GetPower":
							getPower = readFunction(in, line, split[1]);
							getPower = writeFunction("int getPower(Battle b, ActivePokemon me, ActivePokemon o)", getPower);
							break;
						case "GetAccuracy":
							getAcc = readFunction(in, line, split[1]);
							getAcc = writeFunction("int getAccuracy(Battle b, ActivePokemon me, ActivePokemon o)", getAcc);
							break;
						case "GetType":
							getType = "\t\t\tif (user.hasAbility(\"Normalize\")) return Type.NORMAL;\n";
							getType += readFunction(in, line, split[1]);
							getType = writeFunction("Type getType(Battle b, ActivePokemon user)", getType);
							break;
						case "IsMultiTurn":
							isMultiTurn = readFunction(in, line, split[1]);
							isMultiTurn = writeFunction("boolean isMultiTurn(Battle b)", isMultiTurn);
							break;
						case "PhysicalContact":
							physicalContact = split[1].toLowerCase();
							break;
						default:
							Global.error(split[0] + " is not a valid move field declaration (Move: " + name + ")"); // SUCKS TO SUCK
							break;
					}
				}
				else
				{
					switch (field)
					{
						case "effects":
							fields += "\t\t\tsuper." + field + ".add(Effect.getEffect(\"" + mcsplit[1] + "\", EffectType." + mcsplit[0].toUpperCase() + "));\n"; // WE'RE LISTS SO WE'RE SPECIAL AND IMPORTANT OBVIOUSLY LOVE DAT ADD
							break;
						case "moveTypes":
							Attack.validMoveType(split[1]);
							if (split[1].equals("Field")) fieldMove = true;
							if (split[1].equals("NoMagicCoat")) bouncy = true;
							if (split[1].equals("OneHitKO"))
							{
								applyDamage = "\t\t\tif (me.getLevel() < o.getLevel())\n\t\t\t{\n";
								applyDamage += "\t\t\t\tb.addMessage(\"...but it failed!\");\n\t\t\t\treturn -1;\n\t\t\t}\n\n";
								applyDamage += "\t\t\tif (o.hasAbility(\"Sturdy\") && !me.breaksTheMold())\n\t\t\t{\n";
								applyDamage += "\t\t\t\tb.addMessage(o.getName() + \"'s Sturdy prevents OHKO moves!\");\n\t\t\t\treturn -1;\n\t\t\t}\n\n";
								applyDamage += "\t\t\tif (super.zeroAdvantage(b, me, o)) return -1;\n";
								applyDamage += "\t\t\tb.addMessage(\"It's a One-Hit KO!\");\n";
								applyDamage += "\t\t\treturn b.applyDamage(o, o.getHP());\n";
								applyDamage = writeFunction("int applyDamage(ActivePokemon me, ActivePokemon o, Battle b)", applyDamage);
								getAcc = "\t\t\treturn super.accuracy + (me.getLevel() - o.getLevel());\n";
								getAcc = writeFunction("int getAccuracy(Battle b, ActivePokemon me, ActivePokemon o)", getAcc);
							}
							fields += "\t\t\tsuper." + field + ".add(\"" + split[1] + "\");\n"; // WE'RE LISTS SO WE'RE SPECIAL AND IMPORTANT OBVIOUSLY LOVE DAT ADD
							break;
						case "selfTarget":
							fields += "\t\t\tsuper." + field + " = true;\n";
							selfTarget = true;
							if (!split[1].equals("True")) Global.error("SelfTarget must be True if specified (Move: " + name + ")");
							break;
						case "printCast":
							fields += "\t\t\tsuper." + field + " = false;\n";
							if (!split[1].equals("False")) Global.error("PrintCast must be False if specified (Move: " + name + ")");
							break;
						case "pp":
							pp = split[1];
							break;
						case "desc":
							desc = "\"" + split[1] + "\"";
							break;
						case "power":
							power = true;
							fields += "\t\t\tsuper." + field + " = " + split[1] + ";\n"; // DON'T QUOTE ME I'M JUST AN INTEGER!
							break;
						case "accuracy":
							accuracy = true;
						case "priority":
						case "effectChance":
							fields += "\t\t\tsuper." + field + " = " + split[1] + ";\n"; // DON'T QUOTE ME I'M JUST AN INTEGER!
							break;
						case "type":
							type = "Type." + split[1].toUpperCase();
							break;
						case "category":
							category = "Category." + split[1].toUpperCase();
							break;
						case "status":
							fields += "\t\t\tsuper.status = StatusCondition." + split[1].toUpperCase() + ";\n";
							break;
						case "statChanges":
							for (int i = 0, index = 1; i < Integer.parseInt(mcsplit[0]); i++)   
							{
								fields += "\t\t\tsuper.statChanges[Stat." + mcsplit[index++].toUpperCase() + ".index()] = " + mcsplit[index++] + ";\n";
							}
							break;
						case "name":
							name = split[1];
							break;
						default:
							fields += "\t\t\tsuper." + field + " = \"" + split[1] + "\";\n"; // QUOTEY MCQUOTESTER
							break;
					}
				}
				line = in.nextLine().trim();
			}
			
			if (charge.length() > 0) 
			{
				charge = writeFunction("void charge(ActivePokemon user, Battle b)", charge);
			}
			
			if (physicalContact.length() > 0 && !physicalContact.equals("true") && !physicalContact.equals("false")) Global.error("True and false are the only valid fields for physical contact (Move " + name + ")");
			if (physicalContact.length() > 0 && category.contains("STATUS")) Global.error("Status moves never make physical contact (Move " + name + ")");
			if (physicalContact.equals("true") && category.contains("PHYSICAL")) Global.error("Physical moves have implied physical contact (Move " + name + ")");
			if (physicalContact.equals("false") && category.contains("SPECIAL")) Global.error("Special moves have implied no physical contact (Move " + name + ")");
			
			if (physicalContact.equals("true") || (category.contains("PHYSICAL") && !physicalContact.equals("false")))
			{
				fields += "\t\t\tsuper.moveTypes.add(\"PhysicalContact\");\n";
			}
			
			classes.append("\tprivate static class " + className + " extends Attack " + interfaces + "\n\t{\n");
			classes.append("\t\tprivate static final long serialVersionUID = 1L;\n");
			classes.append(extraFields + (extraFields.length() > 0 ? "\n" : ""));
			classes.append("\t\tpublic " + className + "()\n\t\t{\n");
			classes.append("\t\t\tsuper(\"" + name + "\", " + desc + ", " + pp + ", " + type + ", " + category + ");\n");
			classes.append(fields);
			classes.append("\t\t}\n");
			 
			// Additional methods
			classes.append(applyEffects);
			classes.append(getPower);
			classes.append(isMultiTurn);
			classes.append(recoil);
			classes.append(selfHealing);
			classes.append(multiTurn);
			classes.append(charge);
			classes.append(applyDamage);
			classes.append(apply);
			classes.append(changeType);
			classes.append(changeAbility);
			classes.append(abilityMessage);
			classes.append(getAcc);
			classes.append(stageChange);
			classes.append(getType);
			classes.append(statSwitch);
			classes.append(crashDamage);
			classes.append(startTurn);
			classes.append(getPriority);
			classes.append(getItem);
			
			classes.append("\t}\n\n");
			out.append("\t\tmap.put(\"" + name + "\", new " + className + "());\n");
			
			if (category.contains("STATUS") && (power || getPower.length() > 0 || applyDamage.length() > 0)) Global.error("Status moves should not have a power (Move " + name + ")");
			if (!category.contains("STATUS") && !power && getPower.length() == 0 && applyDamage.length() == 0 && apply.length() == 0) System.err.println(name + " does not have a power.");
			if (!selfTarget && !fieldMove && !bouncy && !accuracy && getAcc.length() == 0 
					&& !name.equals("Struggle") && !name.equals("ConfusionDamage")
					&& !name.equals("Aerial Ace") && !name.equals("Swift") 
					&& !name.equals("Mean Look") && !name.equals("Foresight")
					&& !name.equals("Faint Attack") && !name.equals("Odor Sleuth")
					&& !name.equals("Miracle Eye") && !name.equals("Telekinesis")
					&& !name.equals("Vital Throw") && !name.equals("Curse")
					&& !name.equals("Yawn") && !name.equals("Heal Pulse")
					&& !name.equals("Magnet Bomb") && !name.equals("Shadow Punch")
					&& !name.equals("Trump Card") && !name.equals("Clear Smog") 
					&& !name.equals("Magical Leaf") && !name.equals("Shock Wave")
					&& !name.equals("Block") && !name.equals("Aura Sphere")
					&& !name.equals("Spider Web") && !name.equals("Sketch")
					&& !name.equals("Defog") && !name.equals("Bestow")
					&& !name.equals("Perish Song")) System.err.println(name + " does not have accuracy.");
		}
		
		out.append("\n\t\tfor (String s : map.keySet()) moveNames.add(s);\n");
		out.append("\t}\n\n");
		out.append("\t/**** WARNING DO NOT PUT ANY VALUABLE CODE HERE IT WILL BE DELETED *****/\n\n"); // DON'T DO IT
		out.append(classes + "}");
		printToFile(MOVE_PATH, out);
	}
	
	private static void abilityGen()
	{
		Scanner in = openFile("Abilities.txt"), abilities = openFile(ABILITY_PATH);
		StringBuilder out = new StringBuilder(), classes = new StringBuilder(), temp = new StringBuilder();
		while (abilities.hasNext())
		{
			String line = abilities.nextLine();
			out.append(line + "\n");
			if (line.contains("// EVERYTHING BELOW IS GENERATED ###")) break;
		}
		abilities.close();
		temp.append(out);
		
		out.append("\n\t\t// List all of the abilities we are loading\n");
		while (in.hasNext())
		{
			String line = in.nextLine().trim();
			while (in.hasNext() && (line.equals("") || line.charAt(line.length() - 1) != ':'))
			{
				line = in.nextLine().trim();
			}
			
			String className = line.substring(0, line.length() - 1);
			
			line = in.nextLine().trim();
			String name = className, desc = "", interfaces = "", extraFields = "", getMultiplier = "",
					activate = "", statChanging = "", apply = "", effectBlocker = "", prevent = "",
					preventMessage = "", stageChange = "", enter = "", contact = "", block = "",
					alternateEffect = "", applyEffect = "", canAttack = "", opposingCanAttack = "",
					trapped = "", statusPrevent = "", statusPreventMessage = "", deathwish = "",
					changeAbility = "", abilityMessage = "", switchOut = "", changeType = "",
					modifyStageValue = "", getOppMultiplier = "", brace = "", braceMessage = "",
					takeDamage = "", getItem = "", endBattle = "", trapMessage = "", weatherBlock = "";
			
			boolean implemented = false;
			while (in.hasNext() && !line.equals("*"))
			{
				String[] split = line.split(":", 2);
				split[1] = split[1].trim();

				switch (split[0])
				{
					case "Type":
						if (!effectClasses.containsKey(split[1])) Global.error("Undefined Pokemon Ability Type " + split[1] + ". (Effect: " + name + ")");
						interfaces += (implemented ? ", " : "implements ") + effectClasses.get(split[1]);
						implemented = true;
						break;
					case "GetMultiplier":
						getMultiplier = readFunction(in, line, split[1]);
						getMultiplier = writeFunction("double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)", getMultiplier);
						break;
					case "GetOppMultiplier":
						getOppMultiplier = "\t\t\tif (user.breaksTheMold()) return 1;\n";
						getOppMultiplier += readFunction(in, line, split[1]);
						getOppMultiplier = writeFunction("double getOppMultiplier(Battle b, ActivePokemon user, ActivePokemon victim)", getOppMultiplier);
						break;
					case "Activate":
						activate = readFunction(in, line, split[1]);
						activate = writeFunction(className + " newInstance()", "\t\t\t" + className + " x = (" + className + ")(new " + className + "().activate());\n" + activate + "\t\t\treturn x;\n");
						break;
					case "Desc":
						desc = "\"" + split[1] + "\"";
						break;
					case "Modify":
						statChanging = "\t\t\tif (!s.user() && opp.breaksTheMold()) return stat;\n";
						statChanging = readFunction(in, line, split[1]);
						statChanging = writeFunction("int modify(int stat, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)", statChanging);
						break;
					case "SwitchOut":
						switchOut = readFunction(in, line, split[1]);
						switchOut = writeFunction("void switchOut(ActivePokemon switchee)", switchOut);
						break;
					case "EndBattle":
						endBattle = readFunction(in, line, split[1]);
						endBattle = writeFunction("void afterBattle(Trainer player, Battle b, ActivePokemon p)", endBattle);
						break;
					case "GetItem":
						getItem = readFunction(in, line, split[1]);
						getItem = writeFunction("Item getItem()", getItem);
						break;
					case "ModifyStageValue":
						modifyStageValue = readFunction(in, line, split[1]);
						modifyStageValue = writeFunction("int modifyStageValue(int modVal)", modifyStageValue);
						break;
					case "ChangeType":
						changeType = readFunction(in, line, split[1]);
						changeType = writeFunction("Type[] getType(Battle b, ActivePokemon caster, ActivePokemon victim)", changeType);
						break;
					case "AbilityChange":
						changeAbility = readFunction(in, line, split[1]);
						changeAbility = writeFunction("Ability getAbility(Battle b, ActivePokemon caster, ActivePokemon victim)", changeAbility);
						break;
					case "AbilityMessage":
						abilityMessage = readFunction(in, line, split[1]);
						abilityMessage = writeFunction("String getMessage(Battle b, ActivePokemon caster, ActivePokemon victim)", abilityMessage);
						break;
					case "Trapped":
						trapped = readFunction(in, line, split[1]);
						trapped = writeFunction("boolean isTrapped(Battle b, ActivePokemon p)", trapped);
						trapMessage = "\t\t\treturn trapper.getName() + \"'s \" + this.getName() + \" prevents \" + escaper.getName() + \" from escaping!\";\n";
						trapMessage = writeFunction("String trappingMessage(ActivePokemon escaper, ActivePokemon trapper)", trapMessage);
						break;
					case "Bracing":
						brace = readFunction(in, line, split[1]);
						brace = writeFunction("boolean isBracing(Battle b, ActivePokemon bracer, Boolean fullHealth)", brace);
						break;
					case "BraceMessage":
						braceMessage = writeFunction("String braceMessage(ActivePokemon bracer)", "\t\t\treturn " + split[1] + ";\n");
						break;
					case "Apply":
						apply = readFunction(in, line, split[1]);
						apply = writeFunction("void apply(ActivePokemon victim, Battle b)", apply);
						break;
					case "Effect":
						applyEffect = readFunction(in, line, split[1]);
						applyEffect = writeFunction("void applyEffect(Battle b, ActivePokemon user, ActivePokemon victim, Integer damage)", applyEffect);
						break;
					case "TakeDamage":
						takeDamage = readFunction(in, line, split[1]);
						takeDamage = writeFunction("void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim)", takeDamage);
						break;
					case "CanAttack":
						canAttack = readFunction(in, line, split[1]);
						canAttack = writeFunction("boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b)", canAttack);
						break;
					case "OpposingCanAttack":
						opposingCanAttack = "\t\t\tif (p.breaksTheMold()) return true;\n";
						opposingCanAttack += readFunction(in, line, split[1]);
						opposingCanAttack = writeFunction("boolean opposingCanAttack(ActivePokemon p, ActivePokemon opp, Battle b)", opposingCanAttack);
						break;
					case "Name":
						name = split[1];
						break;
					case "ValidMove":
						effectBlocker = "\t\t\tif (user.breaksTheMold()) return true;\n";
						effectBlocker += readFunction(in, line, split[1]);
						effectBlocker = writeFunction("boolean validMove(Battle b, ActivePokemon user, ActivePokemon victim)", effectBlocker);
						break;
					case "Deathwish":
						deathwish = readFunction(in, line, split[1]);
						deathwish = writeFunction("void deathwish(Battle b, ActivePokemon dead, ActivePokemon murderer)", deathwish);
						break;
					case "Prevent":
						prevent = "\t\t\tif (caster.breaksTheMold()) return false;\n";
						prevent += readFunction(in, line, split[1]);
						prevent = writeFunction("boolean prevent(ActivePokemon caster, Stat stat)", prevent);
						break;
					case "PreventMessage":
						preventMessage = writeFunction("String preventionMessage(ActivePokemon p)", "\t\t\treturn " + split[1] + ";\n");
						break;
					case "StatusPrevent":
						statusPrevent = "\t\t\tif (caster.breaksTheMold()) return false;\n";
						statusPrevent += readFunction(in, line, split[1]);
						statusPrevent = writeFunction("boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status)", statusPrevent);
						break;
					case "StatusPreventMessage":
						statusPreventMessage = writeFunction("String preventionMessage(ActivePokemon victim)", "\t\t\treturn " + split[1] + ";\n");
						break;
					case "StageChange":
						stageChange = "\t\t\tif (!s.user() && opp.breaksTheMold()) return stage;\n";
						stageChange += readFunction(in, line, split[1]);
						stageChange = writeFunction("int adjustStage(int stage, Stat s, ActivePokemon p, ActivePokemon opp, Battle b, boolean user)", stageChange);
						break;
					case "Enter":
						enter = readFunction(in, line, split[1]);
						enter = writeFunction("void enter(Battle b, ActivePokemon victim)", enter);
						break;
					case "Contact":
						contact = readFunction(in, line, split[1]);
						contact = writeFunction("void contact(Battle b, ActivePokemon user, ActivePokemon victim)", contact);
						break;
					case "Block":
						block = readFunction(in, line, split[1]);
						block = writeFunction("boolean block(Type attacking, ActivePokemon victim)", block);
						break;
					case "WeatherBlock":
						weatherBlock = readFunction(in, line, split[1]);
						weatherBlock = writeFunction("boolean block(WeatherType weather)", weatherBlock);
						break;
					case "AlternateEffect":
						alternateEffect = readFunction(in, line, split[1]);
						alternateEffect = writeFunction("void alternateEffect(Battle b, ActivePokemon victim)", alternateEffect);
						break;
					case "Field":
						extraFields += readFunction(in, line, split[1], 2);
						break;
					default:
						Global.error(split[0] + " is not a valid ability field declaration (Ability: " + name + ")"); // SUCKS TO SUCK
						break;
				}
				line = in.nextLine().trim();
			}
			
			classes.append("\tprivate static class " + className + " extends Ability " + interfaces + "\n\t{\n");
			classes.append("\t\tprivate static final long serialVersionUID = 1L;\n");
			classes.append(extraFields + (extraFields.length() > 0 ? "\n" : ""));
			classes.append("\t\tpublic " + className + "()\n\t\t{\n");
			classes.append("\t\t\tsuper(\"" + name + "\", " + desc + ");\n\t\t}\n");
			
			// Add newInstance() method
			if (activate.length() > 0) classes.append(activate);
			else classes.append(writeFunction(className + " newInstance()", "\t\t\treturn (" + className + ")(new " + className + "().activate());\n"));
			 
			// Additional methods
			classes.append(getMultiplier);
			classes.append(getOppMultiplier);
			classes.append(statChanging);
			classes.append(apply);
			classes.append(effectBlocker);
			classes.append(modifyStageValue);
			classes.append(prevent);
			classes.append(preventMessage);
			classes.append(statusPrevent);
			classes.append(statusPreventMessage);
			classes.append(stageChange);
			classes.append(enter);
			classes.append(contact);
			classes.append(getItem);
			classes.append(block);
			classes.append(alternateEffect);
			classes.append(applyEffect);
			classes.append(canAttack);
			classes.append(opposingCanAttack);
			classes.append(trapped);
			classes.append(trapMessage);
			classes.append(deathwish);
			classes.append(changeAbility);
			classes.append(changeType);
			classes.append(switchOut);
			classes.append(abilityMessage);
			classes.append(brace);
			classes.append(braceMessage);
			classes.append(takeDamage);
			classes.append(endBattle);
			classes.append(weatherBlock);
			
			classes.append("\t}\n\n");
			out.append("\t\tmap.put(\"" + name + "\", new " + className + "());\n");
		}
		
		out.append("\n\t\tfor (String s : map.keySet()) abilityNames.add(s);\n");
		out.append("\t}\n\n");
		out.append("\t/**** WARNING DO NOT PUT ANY VALUABLE CODE HERE IT WILL BE DELETED *****/\n\n"); // DON'T DO IT
		out.append(classes + "}");
		printToFile(ABILITY_PATH, out);
	}
	
	private static void itemGen()
	{
		Scanner in = openFile(ITEM_PATH);
		StringBuilder orig = new StringBuilder();

		// Read in everything up to the cut off from Item.java
		String s = readLine(in);
		while (s != null && !s.endsWith("// EVERYTHING BELOW IS GENERATED ###"))
		{
			orig.append(s); orig.append('\n');
			s = readLine(in);
		}
		orig.append(s); orig.append('\n');
		
		in.close();
		in = openFile("Items.txt");
		
		ArrayList<String[]> classes = new ArrayList<>();
		
		int index = 0;
		StringBuilder indexOut = new StringBuilder();
		StringBuilder out = new StringBuilder();
		while (in.hasNextLine())
		{
			s = readLine(in);

			if (s.trim().length() == 0 || s.trim().charAt(0) == '#') continue;

			HashMap<String, String> fields = new HashMap<>();
			HashSet<String> interfaces = new HashSet<>();
			String interfaceString = "";
			
			fields.put("Name", s.trim());
			
			s = s.trim().replaceAll("[' \t]+", "");
			fields.put("ClassName", s);
			
			// Reads Key/Value pairs. If there is info on the same line as the key in the input,
			// it assumes that that is the only information, otherwise it will read in the following lines,
			// up to a line which is just "###", used to include full functions.
			String field = readLine(in);
			while (!field.trim().equals("*"))
			{
				String[] tmp = field.split(":");

				if (tmp.length > 1)
				{
					// TODO: Put a check to see if the field is valid
					fields.put(tmp[0].trim(), tmp[1].trim());

					if (tmp[0].trim().equals("Int"))
					{
						interfaceString = tmp[1].trim();
						String[] tmp3 = interfaceString.split(",");
						for (String t : tmp3) interfaces.add(t.trim());
					}
				}
				else
				{
					StringBuilder func = new StringBuilder();
					String tmp2 = readLine(in);
					while (!tmp2.trim().equals("###"))
					{
						func.append(tmp2.trim());
						
						// Sometimes I put '//' comments in Items.txt.
						// This is for those times.
						func.append('\n');

						tmp2 = readLine(in);
					}

					fields.put(tmp[0].trim(), func.toString() + " ");
				}

				field = readLine(in);
			}
			
			// Extend Item if no extend given in input
			if (!fields.containsKey("Ext")) fields.put("Ext", "Item");
			
			if (!fields.containsKey("Name")) fields.put("Name", fields.get("ClassName"));
			classes.add(new String[] {fields.get("Name"), fields.get("ClassName")});

			// Declare class
			out.append("private static class " + fields.get("ClassName") + " ");
			out.append("extends " + fields.get("Ext") + " ");
			
			// Implement everything given in the input, which should all be comma separated
			if (fields.containsKey("Int")) out.append("implements " + fields.get("Int"));

			out.append("{");
			out.append("private static final long serialVersionUID = 1L;");
			if (fields.containsKey("Field")) out.append(fields.get("Field"));
			fields.remove("Field");
			
			// Constructor
			out.append("public " + fields.get("ClassName") + "(){");

			// Constructor - set name/category/etc...
			out.append("super.name = \"" + fields.get("Name") + "\";");

			if (!fields.containsKey("Cat")) Global.error("No BagCategory for " + fields.get("ClassName") + "\n");
			else out.append("super.cat = BagCategory." + fields.get("Cat") + ";");
			
			if (!fields.containsKey("BattleCat"))
			{
				out.append("super.bcat = new BattleBagCategory[0];");
			}
			else 
			{
				out.append("super.bcat = new BattleBagCategory[]{");
				String[] tmp = fields.get("BattleCat").trim().split(",");
				for (String tmp2 : tmp)
					out.append("BattleBagCategory." + tmp2 + ",");
				out.deleteCharAt(out.length() - 1);
				out.append("};");
			}
			
			File ftmp = new File(ITEM_TILES_PATH + fields.get("ClassName").toLowerCase() + ".png");
			if (!ftmp.exists()) System.err.println("Image for " + fields.get("Name") + " does not exist." + ftmp.getAbsolutePath());
			
			String base16idx = Integer.toString(index, 16);
			while (base16idx.length() < 8) base16idx = "0" + base16idx;
			indexOut.append(fields.get("ClassName").toLowerCase() + ".png " + base16idx + "\n");
			out.append("super.index = " + (index++) + ";");
			
			if (!fields.containsKey("Desc")) Global.error("No Description for " + fields.get("ClassName"));
			else out.append("super.desc = \"" + fields.get("Desc") + "\";");
			
			if (!fields.containsKey("Price")) fields.put("Price", "-1");
			out.append("super.price = " + fields.get("Price") + ";}");
			
			boolean successMsg = false;
			if (fields.containsKey("SuccessMessage"))
			{
				successMsg = true;
				out.append("public String getSuccessMessage(ActivePokemon p){ return " + fields.get("SuccessMessage") + "; }");
				fields.remove("SuccessMessage");
			}
			
			if (fields.get("Ext").equals("PowerItem"))
			{
				// Does not throw an error if it does not have this field, because it can be overridden -- like with the Macho Brace
				if (fields.containsKey("PowerStat")) out.append("public Stat toIncrease(){return Stat." + fields.get("PowerStat") + ";}");
				fields.remove("PowerStat");
			}
			
			if (fields.get("Ext").equals("StatusConditionRemoveItem"))
			{
				if (!fields.containsKey("ToRemove")) Global.error(fields.get("ClassName") + " must have a toRemove method, since it implements StatusConditionRemoveItem.");
				else out.append("public StatusCondition toRemove(){return StatusCondition." + fields.get("ToRemove") + ";}");
				fields.remove("ToRemove");
			}

			if (fields.get("Ext").equals("HealItem"))
			{
				if (!fields.containsKey("HealAmt")) Global.error(fields.get("ClassName") + " must have an healAmt method, since it implements HealItem.");
				else out.append("public int healAmt(){return " + fields.get("HealAmt") + ";}");
				fields.remove("HealAmt");
			}
			
			// If it's a TypeItem, we need a type
			if (fields.get("Ext").equals("TypeEnhancingItem"))
			{
				if (!fields.containsKey("Type")) Global.error(fields.get("ClassName") + " must have a getType method, since it implements HoldItem.");
				else out.append("public Type getType(){return Type." + fields.get("Type") + ";}");
				fields.remove("Type");
			}
			
			// The corresponding ACs implement StatIncreaseItem, the generator just adds them as interfaces
			if (fields.get("Ext").equals("EVIncreaseItem") || fields.get("Ext").equals("StageIncreaseItem"))
			{
				if (!fields.containsKey("ToIncrease")) Global.error(fields.get("ClassName") + " must have a toIncrease method, since it implements StatIncreaseItem.");
				else 
				{
					out.append("public Stat toIncrease(){return ");
					if (fields.get("ToIncrease").equals("null")) out.append("null");
					else out.append("Stat." + fields.get("ToIncrease"));
					out.append(";}");
				}
				fields.remove("ToIncrease");
			}
			
			// If it's a vitamin, we need an increaseAmt method
			if (fields.get("Ext").equals("EVIncreaseItem"))
			{
				if (!fields.containsKey("IncreaseAmt")) Global.error(fields.get("ClassName") + " must have an increaseAmt method, since it implements EVIncreaseItem.");
				else out.append("public int increaseAmt(){return " + fields.get("IncreaseAmt") + ";}");
				fields.remove("IncreaseAmt");
			}
			
			if (fields.get("Ext").equals("EVDecreaseBerry"))
			{
				if (!fields.containsKey("EVToDecrease")) Global.error(fields.get("ClassName") + " must have a EVToDecrease method, because it implements EVDecreaseBerry.");
				else out.append("public Stat toDecrease(){return Stat." + fields.get("EVToDecrease") + ";}");
				fields.remove("EVToDecrease");
			}
			
			if (fields.get("Ext").equals("SuperEffectivePowerReduceBerry"))
			{
				if (!fields.containsKey("SuperEffectivePowerReduceBerryType")) Global.error(fields.get("ClassName") + " must have a SuperEffectivePowerReduceBerryType field, because it implements SuperEffectivePowerReduceBerry.");
				else out.append("public Type getType(){return Type." + fields.get("SuperEffectivePowerReduceBerryType") + ";}");
				fields.remove("SuperEffectivePowerReduceBerryType");
			}
			
			if (fields.get("Ext").equals("HealthTriggeredStageIncreaseBerry"))
			{
				if (!fields.containsKey("HealthTriggeredStageIncreaseBerryStat")) Global.error(fields.get("ClassName") + " must have a HealthTriggeredStageIncreaseBerryStat field, because it implements HealthTriggeredStageIncreaseBerry.");
				else out.append("public Stat toRaise(){return Stat." + fields.get("HealthTriggeredStageIncreaseBerryStat") + ";}");
				fields.remove("HealthTriggeredStageIncreaseBerryStat");
			}
			
			if (fields.get("Ext").equals("ChoiceItem"))
			{
				if (!fields.containsKey("ChoiceStat")) Global.error(fields.get("ClassName") + " must have a ChoiceStat method, because it extends ChoiceItem.");
				else out.append("public Stat toIncrease() { return Stat." + fields.get("ChoiceStat") + "; }");
				fields.remove("ChoiceStat");
			}
			
			if (fields.get("Ext").equals("RepelItem"))
			{
				if (!fields.containsKey("RepelSteps")) Global.error(fields.get("ClassName" + " must have a RepelSteps method, because it extends RepelItem."));
				else out.append("public int repelSteps() { return " + fields.get("RepelSteps") + "; }");
				fields.remove("RepelSteps");
			}
			
			if (fields.get("Ext").equals("TypeDamageStatIncreaseItem"))
			{
				if (!fields.containsKey("DamageType")) Global.error(fields.get("ClassName") + " must have a DamageType method, because it extends TypeDamageStatIncreaseItem.");
				else out.append("public Type damageType() { return Type." + fields.get("DamageType") + "; }");
				fields.remove("DamageType");
				
				if (!fields.containsKey("IncreaseStat")) Global.error(fields.get("ClassName") + " must have a IncreaseStat method, because it extends TypeDamageStatIncreaseItem.");
				else out.append("public Stat toIncrease() { return Stat." + fields.get("IncreaseStat") + "; }");
				fields.remove("IncreaseStat");
			}
			
			// If it's a TypeEnhancingItem, we need a multiplier
			if (fields.get("Ext").equals("TypeEnhancingItem"))
			{
				if (!fields.containsKey("Multiplier")) Global.error(fields.get("ClassName") + " must have a getMultiplier method, since it implements TypeEnhancingItem.");
				else out.append("public double getMultiplier(){return " + fields.get("Multiplier") + ";}");
				fields.remove("Multiplier");
			}
			
			// If it's a EndTurnEffect, we need an apply method
			if (interfaces.contains("EndTurnEffect"))
			{
				if (!fields.containsKey("Apply")) Global.error(fields.get("ClassName") + " must have an apply method, since it implements EndTurnEffect.");
				else out.append("public void apply(ActivePokemon victim, Battle b){" + fields.get("Apply") + "}");
				fields.remove("Apply");
			}

			// If it's a BattleUseItem, we need a use method that takes in a battle
			if (interfaces.contains("BattleUseItem"))
			{
				if (!fields.containsKey("BattleUse"))
				{
					// Some BattleUse methods are the same inside and outside of battle, so just use the outside of battle one
					if (fields.containsKey("PokemonUse")) out.append("public boolean use(ActivePokemon p, Battle b){ return use(p); }"); 
					else Global.error(fields.get("ClassName") + " must have a use method, since it implements BattleUseItem.");
				}
				else out.append("public boolean use(ActivePokemon p, Battle b){" + fields.get("BattleUse") + "}");
				fields.remove("BattleUse");
			}
			
			// If it's a Use, we need a use method, but allow it to be overwritten if specified (allows subclasses of some ACs to override their use method)
			if (interfaces.contains("PokemonUseItem") && !fields.containsKey("PokemonUse")) Global.error(fields.get("ClassName") + " must have a use method, since it implements PokemonUseItem.");
			if (fields.containsKey("PokemonUse")) out.append("public boolean use(ActivePokemon p){" + fields.get("PokemonUse") + "}");
			fields.remove("PokemonUse");
			
			// If it's a TrainerUseItem, we need a use method that takes in a trainer
			if (interfaces.contains("TrainerUseItem"))
			{
				if (!fields.containsKey("TrainerUse")) Global.error(fields.get("ClassName") + " must have a use method, since it implements TrainerUseItem.");
				else out.append("public boolean use(Trainer t){" + fields.get("TrainerUse") + "}");
				fields.remove("TrainerUse");
			}
			
			// If it's a MoveUseItem, we need a use method that takes in a trainer
			if (interfaces.contains("MoveUseItem"))
			{
				if (!fields.containsKey("MoveUse")) Global.error(fields.get("ClassName") + " must have a use method, since it implements MoveUseItem.");
				else out.append("public boolean use(Move m){" + fields.get("MoveUse") + "}");
				fields.remove("MoveUse");
			}
			
			// If it's a HoldItem, we need fling damage!
			if (interfaces.contains("HoldItem"))
			{
				if (!fields.containsKey("Fling")) Global.error(fields.get("ClassName") + " must have a flingDamage method, since it implements HoldItem.");
				else out.append("public int flingDamage(){return " + fields.get("Fling") + ";}");
				fields.remove("Fling");
			}
			// Some things have extensions that are HoldItems and this is easier than special casing all of these
			else if (fields.containsKey("Fling"))
			{
				out.append("public int flingDamage(){return " + fields.get("Fling") + ";}");
				fields.remove("Fling");
			}
			
			if (interfaces.contains("StatChangingEffect"))
			{
				if (!fields.containsKey("ModifyStat")) Global.error(fields.get("ClassName") + " must have a ModifyStat field, since it implements StatChangingEffect.");
				else out.append("public int modify(int stat, ActivePokemon p, ActivePokemon opp, Stat s, Battle b){" + fields.get("ModifyStat") + "}");
				fields.remove("ModifyStat");
			}
			
			if (interfaces.contains("WeatherExtendingEffect"))
			{
				if (!fields.containsKey("WeatherType")) Global.error(fields.get("ClassName") + " must have a WeatherType field, since it implements WeatherExtendingEffect.");
				else out.append("public WeatherType getWeatherType(){ return WeatherType." + fields.get("WeatherType").toUpperCase() + "; }");
				fields.remove("WeatherType");
			}
			
			if (interfaces.contains("CritStageEffect"))
			{
				if (!fields.containsKey("IncreaseCritStage")) Global.error(fields.get("ClassName") + " must have a WeatherType field, since it implements CritStageEffect.");
				else out.append("public int increaseCritStage(ActivePokemon p){ " + fields.get("IncreaseCritStage") + "}");
				fields.remove("IncreaseCritStage");
			}
			
			if (interfaces.contains("AttackSelectionEffect"))
			{
				if (!fields.containsKey("Usable")) Global.error(fields.get("ClassName") + " must have a Usable field, since it implements AttackSelectionEffect.");
				else out.append("public boolean usable(ActivePokemon p, Move m){ " + fields.get("Usable") + "}");
				fields.remove("Usable");
				
				if (!fields.containsKey("UnusableMessage")) Global.error(fields.get("ClassName") + " must have a UnusableMessage field, since it implements AttackSelectionEffect.");
				else out.append("public String getUnusableMessage(ActivePokemon p){ " + fields.get("UnusableMessage") + "}");
				fields.remove("UnusableMessage");
			}
			
			// If it's a ball, we need a catchRate and afterCaught methods
			if (interfaces.contains("BallItem"))
			{
				if (!fields.containsKey("CatchRate")) Global.error(fields.get("ClassName") + " must have an catchRate method, since it implements BallItem.");
				else out.append("public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b){" + fields.get("CatchRate") + "}");
				fields.remove("CatchRate");
				
				// If an AfterCaught field is not present, just make an empty one (doesn't matter)
				if (!fields.containsKey("AfterCaught")) out.append("public void afterCaught(ActivePokemon p){return;}");
				else out.append("public void afterCaught(ActivePokemon p){" + fields.get("AfterCaught") + "}");
				fields.remove("AfterCaught");
			}
						
			// If it's a berry, we need the natural gift thingies
			if (interfaceString.contains("Berry") || (fields.get("Ext").contains("Berry")))
			{
				if (!fields.containsKey("NGType")) Global.error(fields.get("ClassName") + " must have a naturalGiftType method, since it implements Berry.");
				else out.append("public Type naturalGiftType(){return Type." + fields.get("NGType") + ";}");
				fields.remove("NGType");
				
				if (!fields.containsKey("NGPow")) Global.error(fields.get("ClassName") + " must have a naturalGiftPower method, since it implements Berry.");
				else out.append("public int naturalGiftPower(){return " + fields.get("NGPow") + ";}");
				fields.remove("NGPow");
			}
			
			if (interfaces.contains("PowerChangeEffect"))
			{
				if (!fields.containsKey("PCMultiplier")) Global.error(fields.get("ClassName") + " must have a PCMultiplier field, since it implements PowerChangeEffect.");
				else out.append("public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim){" + fields.get("PCMultiplier") + "}");
				fields.remove("PCMultiplier");
			}
			
			if (interfaces.contains("EntryEffect"))
			{
				if (!fields.containsKey("Enter")) Global.error(fields.get("ClassName") + " must have a enter method, since it implements EntryEffect.");
				else out.append("public void enter(Battle b, ActivePokemon victim){" + fields.get("Enter") + "}");
				fields.remove("Enter");
			}

			if (interfaces.contains("EffectBlockerEffect"))
			{
				if (!fields.containsKey("ValidMove")) Global.error(fields.get("ClassName") + " must have a ValidMove method, since it implements EffectBlockerEffect.");
				else out.append("public boolean validMove(Battle b, ActivePokemon user, ActivePokemon victim){" + fields.get("ValidMove") + "}");
				fields.remove("ValidMove");
			}
			
			if (interfaces.contains("WeatherBlockerEffect"))
			{
				if (!fields.containsKey("WeatherBlock")) Global.error(fields.get("ClassName") + " must have a WeatherBlock method, since it implements WeatherBlockerEffect.");
				else out.append("public boolean block(WeatherType weather){" + fields.get("WeatherBlock") + "}");
				fields.remove("WeatherBlock");
			}
			
			// If it's a TypeItem, we need a type
			if (interfaces.contains("DriveItem"))
			{
				if (!fields.containsKey("Type")) Global.error(fields.get("ClassName") + " must have a getType method, since it implements DriveItem.");
				else out.append("public Type getType(){return Type." + fields.get("Type") + ";}");
				fields.remove("Type");
			}
			
			if (interfaces.contains("RepellingEffect"))
			{
				if (!fields.containsKey("RepelChance")) Global.error(fields.get("ClassName") + " must have a RepelChance method, because it implements RepellingEffect.");
				else out.append("public double chance() { return " + fields.get("RepelChance") + "; }");
				fields.remove("RepelChance");
			}
			
			if (interfaces.contains("BracingEffect"))
			{
				if (!fields.containsKey("Bracing")) Global.error(fields.get("ClassName") + " must have a isBracing method, since it implements BracingEffect.");
				else out.append("public boolean isBracing(Battle b, ActivePokemon bracer, Boolean fullHealth){" + fields.get("Bracing") + "}");
				fields.remove("Bracing");
				
				if (!fields.containsKey("BraceMessage")) Global.error(fields.get("ClassName") + " must have a braceMessage method, since it implements BracingEffect.");
				else out.append("public String braceMessage(ActivePokemon bracer){" + fields.get("BraceMessage") + "}");
				fields.remove("BraceMessage");
			}
			
			if (interfaces.contains("ApplyDamageEffect"))
			{
				if (!fields.containsKey("OnApplyDamage")) Global.error(fields.get("ClassName") + " must have an OnApplyDamage field, since it implements ApplyDamageEffect.");
				else out.append("public void applyEffect(Battle b, ActivePokemon user, ActivePokemon victim, Integer damage){" + fields.get("OnApplyDamage") + "}");
				fields.remove("OnApplyDamage");
			}
			
			if (interfaces.contains("TakeDamageEffect"))
			{
				if (!fields.containsKey("OnTakeDamage")) Global.error(fields.get("ClassName") + " must have an OnTakeDamage field, since it implements TakeDamageEffect.");
				else out.append("public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim){" + fields.get("OnTakeDamage") + "}");
				fields.remove("OnTakeDamage");
			}
			
			if (interfaces.contains("PhysicalContactEffect"))
			{
				if (!fields.containsKey("OnContact")) Global.error(fields.get("ClassName") + " must have an OnContact field, since it implements PhysicalContactEffect.");
				else out.append("public void contact(Battle b, ActivePokemon user, ActivePokemon victim){" + fields.get("OnContact") + "}");
				fields.remove("OnContact");
			}
			
			if (interfaces.contains("StatProtectingEffect"))
			{
				if (!fields.containsKey("StatProtect")) Global.error(fields.get("ClassName") + " must have an StatProtect field, since it implements PhysicalContactEffect.");
				else out.append("public boolean prevent(ActivePokemon caster, Stat stat){" + fields.get("StatProtect") + "}");
				fields.remove("StatProtect");
				
				if (!fields.containsKey("StatProtectMessage")) Global.error(fields.get("ClassName") + " must have an StatProtectMessage field, since it implements PhysicalContactEffect.");
				else out.append("public String preventionMessage(ActivePokemon p){" + fields.get("StatProtectMessage") + "}");
				fields.remove("StatProtectMessage");
			}

			if (interfaces.contains("ItemCondition"))
			{
				if (!fields.containsKey("GetItem")) Global.error(fields.get("ClassName") + " must have a GetItem field, since it implements ItemCondition.");
				else out.append("public Item getItem(){" + fields.get("GetItem") + "}");
				fields.remove("GetItem");
			}
			
			if (interfaces.contains("GainableEffectBerry"))
			{
				if (!fields.containsKey("GainEffect")) Global.error(fields.get("ClassName") + " must have a GainEffect field, since it implements GainableEffectBerry.");
				else out.append("public void useBerry(Battle b, ActivePokemon user, ActivePokemon opp){" + fields.get("GainEffect") + "}");
				fields.remove("GainEffect");
			}
			
			if (interfaces.contains("StatusBerry"))
			{
				out.append("public void useBerry(Battle b, ActivePokemon user, ActivePokemon opp){ if (use(user, b)) b.addMessage(getSuccessMessage(user), user.getStatus().getType(), user.user()); }");
			}
			
			if (interfaces.contains("HealthTriggeredBerry"))
			{
				if (!fields.containsKey("HealthTrigger")) Global.error(fields.get("ClassName") + " must have a HealthTrigger field, since it implements HealthTriggeredBerry.");
				else out.append("public boolean useHealthTriggerBerry(Battle b, ActivePokemon user){" + fields.get("HealthTrigger") + "}");
				fields.remove("HealthTrigger");
				
				if (!fields.containsKey("HealthTriggerRatio")) Global.error(fields.get("ClassName") + " must have a HealthTriggerRatio field, since it implements HealthTriggeredBerry.");
				else out.append("public double healthTriggerRatio(){" + fields.get("HealthTriggerRatio") + "}");
				fields.remove("HealthTriggerRatio");
				
				out.append("public void useBerry(Battle b, ActivePokemon user, ActivePokemon opp){ useHealthTriggerBerry(b, user); }");
			}
			
			out.append("}");
			
			fields.remove("Name");
			fields.remove("Cat");
			fields.remove("Ext");
			fields.remove("Int");
			fields.remove("Price");
			fields.remove("Desc");
			fields.remove("BattleCat");
			
			for (String entry : fields.keySet())
			{
				if (entry.equals("ClassName")) continue;
				System.err.println(fields.get("ClassName") + " has unused field " + entry);
			}
		}
		out.append("}");
		
		printToFile(ITEM_TILES_PATH + "index.txt", indexOut);
		
		StringBuilder puts = new StringBuilder();
		for (String[] cl : classes) puts.append("map.put(\"" + cl[0] + "\", new " + cl[1] + "());");
		puts.append("}");
		
		printToFile(ITEM_PATH, orig.append(puts).append(out));
	}
	
	private static String readLine(Scanner in)
	{
		String res = in.nextLine();
		
		if (res.length() > 0 && res.charAt(res.length() - 1) == '\n') 
		{
			res = res.substring(0, res.length() - 1);
		}
		
		return res;
	}
	
	private static String addTabs(int tabs)
	{
		String s = "";
		
		for (int i = 0; i < tabs; i++) 
			s += "\t";
		
		return s;
	}
	
	public static void printToFile(String fileName, StringBuilder out)
	{
		try
		{
			new PrintStream(new File(fileName)).println(out);
		}
		catch (FileNotFoundException ex)
		{
			Global.error("STUPIDNESS");
		}
	}
	
	public static Scanner openFile(String fileName) 
	{
		Scanner in = null;
		try
		{
			in = new Scanner(new File(fileName));
		}
		catch (FileNotFoundException ex)
		{
			Global.error(fileName + " not found");
		}
		
		return in;
	}
	
	public static String readFunction(Scanner in, String line, String curLine)
	{
		return readFunction(in, line, curLine, 3);
	}
	
	public static String readFunction(Scanner in, String line, String curLine, int tabs)
	{
		String s = "";
		if (curLine.length() > 0) 
			s = addTabs(tabs) + curLine + "\n";
		
		line = in.nextLine().trim();
		
		while (in.hasNext() && !line.equals("###")) 
		{
			if (line.contains("}") && !line.contains("{")) 
				tabs--;
			
			s += addTabs(tabs) + line + "\n";
			
			if (line.contains("{") && !line.contains("}")) 
				tabs++;
			
			line = in.nextLine().trim();
		}
		
		return s;
	}
	
	public static String writeFunction(String header, String body)
	{
		return "\n\t\tpublic " + header + "\n\t\t{\n" + body + "\t\t}\n";
	}
	
	// Stuff that shouldn't necessarily be in this file and isn't really used but has nowhere else to live
	private static ArrayList<File> files;
	
	private static void plus()
	{
		files = new ArrayList<File>();
		addFiles(new File("C:\\Users\\leahf_000\\Documents\\Pokemon++\\src"));
		
		for (File f : files)
		{
			Pattern p = Pattern.compile("[^ +\t]\\ + [^ +=]");
			StringBuilder out = new StringBuilder();
			
			Scanner in = null;
			try
			{
				in = new Scanner(f);
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
			
			while (in.hasNext())
			{
				String line = in.nextLine();
				
				if (!line.contains("Pattern.compile"))
				{
					Matcher m = p.matcher(line);
					
					while (m.find())
					{
						String group = m.group();
						String replace = m.group().replace("+", " + ");
						
						line = line.replace(group, replace);
						System.out.println(line);
					}
				}
				
				out.append(line + "\n");
			}
			
			out = new StringBuilder(out.substring(0, out.length() - 1));
			
			printToFile(f.getAbsolutePath(), out);
//			System.out.println(f.getAbsolutePath() + "\n" + out);
		}
	}
	
	private static void longestString()
	{
		files = new ArrayList<File>();
		addFiles(new File("C:\\Users\\!\\Documents\\GitHub\\Pokemon\\src"));
		
		String max = "";
		for (File f : files)
		{
			Pattern p = Pattern.compile("addMessage[^,]*;");
			
			Scanner in = null;
			try
			{
				in = new Scanner(f);
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
			
			while (in.hasNext())
			{
				String line = in.nextLine();
				Matcher m = p.matcher(line);
				while (m.find())
				{
					String temp = line.substring(m.start() + 11, m.end() - 2);
					if (temp.length() > max.length())
						max = temp;
				}
			}
		}
		
		System.out.println(max.length() + " " + max);
	}
	
	private static void addFiles(File f)
	{
		File[] list = f.listFiles();

		for (int i = 0; i < list.length; i++)
		{
			if (list[i].isFile())
			{
				files.add(list[i]);
			}
			else
			{
				addFiles(list[i]);
			}
		}
	}
	
	// Used for editing pokemoninfo.txt
	private static void pokemonInfoStuff()
	{
		Scanner in = openFile("pokemoninfo.txt");
		PrintStream out = null;
		try {
			out = new PrintStream("out.txt");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		while (in.hasNext())
		{
			out.println(in.nextLine()); // Num
			out.println(in.nextLine()); // Name
			out.println(in.nextLine()); // Base Stats
			out.println(in.nextLine()); // Base Exp
			out.println(in.nextLine()); // Growth Rate
			out.println(in.nextLine()); // Type1 Type2			
			readMoves(in, out); // Level Up Moves
			out.println(in.nextLine()); // Catch Rate
			out.println(in.nextLine()); // EVs
			readEvolution(in, out); // Evolution  
			readHoldItems(in, out); // Wild Items
			out.println(in.nextLine()); // Male Ratio
			out.println(in.nextLine()); // Ability 1
			out.println(in.nextLine()); // Ability 2
			out.println(in.nextLine()); // Classification
			out.println(in.nextLine()); // Height Weight FlavorText
			out.println(in.nextLine()); // Egg Steps
			out.println(in.nextLine()); // Egg Group 1
			out.println(in.nextLine()); // Egg Group 2
			
			out.println(in.nextLine()); // New Line
		}
	}
	
	private static void readMoves(Scanner in, PrintStream out)
	{
		int numMoves = in.nextInt();
		out.println(numMoves); // Number of Moves 
		in.nextLine();
		for (int i = 0; i < numMoves; i++) out.println(in.nextLine()); // Each move and level
	}
	
	private static void readEvolution(Scanner in, PrintStream out)
	{
		String type = in.next();
		if (type.equals("Multi"))
		{
			int x = in.nextInt();
			out.println(type + " " + x);
			for (int i = 0; i < x; i++) readEvolution(in, out);
			return;
		}
		out.println(type + " " + in.nextLine());
	}
	
	private static void readHoldItems(Scanner in, PrintStream out)
	{
		int num = in.nextInt();
		out.println(num);
		in.nextLine();
		for (int i = 0; i < num; i++) out.println(in.nextLine());
	}
}
