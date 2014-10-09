package pokemon;

import item.Item;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import main.Global;
import main.Namesies;
import battle.Attack;
import battle.Move;

public abstract class Evolution implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	public enum EvolutionCheck
	{
		LEVEL, ITEM, MOVE
	}
	
	public abstract Evolution getEvolution(EvolutionCheck type, ActivePokemon p, Namesies use);
	public abstract Namesies[] getEvolutions();
	
	public boolean canEvolve()
	{
		return true;
	}

	public static Evolution readEvolution(Scanner in)
	{
		String type = in.next();
		switch (type)
		{
			case "None":
				return new NoEvolution();
			case "Gender":
				return new GenderEvolution(in.next(), readEvolution(in));
			case "Stat":
				return new StatEvolution(in.next(), in.next(), in.next(), readEvolution(in));
			case "Level":
				return new LevelUpEvolution(in.nextInt(), in.nextInt());
			case "Item":
				return new ItemEvolution(in.nextInt(), in.nextLine().trim());
			case "Move":
				return new MoveEvolution(in.nextInt(), in.nextLine().trim());
			case "Multi":
				Evolution[] evolutions = new Evolution[in.nextInt()];
				for (int i = 0; i < evolutions.length; i++) 
				{
					evolutions[i] = readEvolution(in);
				}
				
				return new MultipleEvolution(evolutions);
			default:
				Global.error("Undefined Evolution Type " + type);
				return null; // THIS SHOULDN'T EVEN GET CALLED BECAUSE I JUST DID A SYSTEM.EXIT WOOOO JAVA
		}
	}
	
	private static class NoEvolution extends Evolution 
	{
		private static final long serialVersionUID = 1L;
		
		public Evolution getEvolution(EvolutionCheck type, ActivePokemon p, Namesies use)
		{
			return null;
		}
		
		public boolean canEvolve()
		{
			return false;
		}

		public Namesies[] getEvolutions()
		{
			return new Namesies[0];
		}
	}
	
	private static class MultipleEvolution extends Evolution 
	{
		private static final long serialVersionUID = 1L;
		
		private Evolution[] evolutions;
		
		public MultipleEvolution(Evolution[] list)
		{
			evolutions = list;
		}
		
		public Evolution getEvolution(EvolutionCheck type, ActivePokemon p, Namesies use)
		{
			List<Evolution> list = new ArrayList<>();
			for (Evolution ev : evolutions)
			{
				Evolution lev = ev.getEvolution(type, p, use);
				if (lev != null) 
				{
					list.add(lev);
				}
			}
			
			int size = list.size();
			if (size > 0) 
			{
				// This is pretty much for Wurmple even though he's not even going in the game
				return list.get((int)(Math.random()*size));
			}
			
			return null;
		}

		public Namesies[] getEvolutions()
		{
			Namesies[] s = new Namesies[evolutions.length];
			for (int i = 0; i < evolutions.length; i++) 
			{
				s[i] = evolutions[i].getEvolutions()[0];		
			}
			
			return s;
		}
	}

	private static class GenderEvolution extends Evolution 
	{
		private static final long serialVersionUID = 1L;
		
		private Evolution evolution;
		private Gender gender;
		
		public GenderEvolution(String g, Evolution e)
		{
			if (!(e instanceof BaseEvolution)) Global.error("Gender evolution does not make any sense!");
			
			evolution = e;
			if (!g.equals("Male") && !g.equals("Female")) Global.error("Incorrect Gender Name for Evoution");
			gender = g.equals("Male") ? Gender.MALE : Gender.FEMALE;
		}
		
		public Evolution getEvolution(EvolutionCheck type, ActivePokemon p, Namesies use)
		{
			if (p.getGender() == gender) return evolution.getEvolution(type, p, use);
			
			return null;
		}

		public Namesies[] getEvolutions()
		{
			return evolution.getEvolutions();
		}
	}

	private static class StatEvolution extends Evolution 
	{
		private static final long serialVersionUID = 1L;
		
		private LevelUpEvolution evolution;
		private boolean equals;
		private Stat higher;
		private Stat lower;
		
		public StatEvolution(String eq, String h, String l, Evolution e)
		{
			if (!(e instanceof LevelUpEvolution)) 
			{
				Global.error("Stat evolutions must be level up");
			}
			
			evolution = (LevelUpEvolution)e;
			equals = eq.equals("Equal");
			
			higher = Stat.valueOf(h.toUpperCase());
			lower = Stat.valueOf(l.toUpperCase());
		}

		public Evolution getEvolution(EvolutionCheck type, ActivePokemon p, Namesies use)
		{
			if (type != EvolutionCheck.LEVEL) 
			{
				return null;
			}
			
			int[] stats = p.getStats();
			int high = stats[higher.index()], low = stats[lower.index()];
			
			if (equals && high == low) return evolution.getEvolution(type, p, use);
			if (high > low) return evolution.getEvolution(type, p, use);
			
			return null;
		}

		public Namesies[] getEvolutions()
		{
			return evolution.getEvolutions();
		}
	}
	
	private static class LevelUpEvolution extends Evolution implements BaseEvolution 
	{
		private static final long serialVersionUID = 1L;
		
		private int evolutionNumber;
		private int level;
		
		public LevelUpEvolution(int num, int level) 
		{
			this.evolutionNumber = num;
			this.level = level;
		}
		
		public PokemonInfo getEvolution()
		{
			return PokemonInfo.getPokemonInfo(evolutionNumber);
		}
		
		public Evolution getEvolution(EvolutionCheck type, ActivePokemon p, Namesies use)
		{
			if (type != EvolutionCheck.LEVEL) 
			{
				return null;
			}
			
			if (p.getLevel() >= level) 
			{
				return this; 
			}
			
			return null;
		}

		public Namesies[] getEvolutions()
		{
			return new Namesies[] { PokemonInfo.getPokemonInfo(evolutionNumber).namesies() };
		}
	}

	private static class ItemEvolution extends Evolution implements BaseEvolution 
	{
		private static final long serialVersionUID = 1L;
		
		private int evolutionNumber;
		private Namesies item;
		 
		public ItemEvolution(int num, String item)
		{
			this.evolutionNumber = num;
			
			if (!Item.isItem(item))
			{
				Global.error("Invalid item name " + item);
			}
			
			this.item = Item.getItemFromName(item).namesies();
		}
		
		public PokemonInfo getEvolution()
		{
			return PokemonInfo.getPokemonInfo(evolutionNumber);
		}
		
		public Evolution getEvolution(EvolutionCheck type, ActivePokemon p, Namesies use)
		{
			if (type != EvolutionCheck.ITEM) 
			{
				return null;
			}
			
			if (use == item) 
			{
				return this;
			}
			
			return null;
		}

		public Namesies[] getEvolutions()
		{
			return new Namesies[] { PokemonInfo.getPokemonInfo(evolutionNumber).namesies() };
		}
	}

	private static class MoveEvolution extends Evolution implements BaseEvolution 
	{
		private static final long serialVersionUID = 1L;
		
		private int evolutionNumber;
		private Namesies move;
		
		public MoveEvolution(int num, String m)
		{
			evolutionNumber = num;
			
			if (!Attack.isAttack(m))
			{
				Global.error("Invalid attack name " + m);
			}
			
			move = Attack.getAttackFromName(m).namesies();
		}
		
		public PokemonInfo getEvolution()
		{
			return PokemonInfo.getPokemonInfo(evolutionNumber);
		}
		
		public Evolution getEvolution(EvolutionCheck type, ActivePokemon p, Namesies use)
		{
			if (type != EvolutionCheck.MOVE) 
			{
				return null;
			}
			
			for (Move m : p.getActualMoves())
			{
				if (m.getAttack().namesies() == move)
				{
					return this;
				}
			}
			
			return null;
		}

		public Namesies[] getEvolutions()
		{
			return new Namesies[] { PokemonInfo.getPokemonInfo(evolutionNumber).namesies() };
		}
	}
}
