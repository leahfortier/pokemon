package pokemon;

import main.Global;
import util.PokeString;

public enum PokemonNamesies {
	NONE(""), // Mostly so the index matches up
    // EVERYTHING BELOW IS GENERATED ###
	BULBASAUR("Bulbasaur"),
	IVYSAUR("Ivysaur"),
	VENUSAUR("Venusaur"),
	CHARMANDER("Charmander"),
	CHARMELEON("Charmeleon"),
	CHARIZARD("Charizard"),
	SQUIRTLE("Squirtle"),
	WARTORTLE("Wartortle"),
	BLASTOISE("Blastoise"),
	CATERPIE("Caterpie"),
	METAPOD("Metapod"),
	BUTTERFREE("Butterfree"),
	WEEDLE("Weedle"),
	KAKUNA("Kakuna"),
	BEEDRILL("Beedrill"),
	PIDGEY("Pidgey"),
	PIDGEOTTO("Pidgeotto"),
	PIDGEOT("Pidgeot"),
	RATTATA("Rattata"),
	RATICATE("Raticate"),
	SPEAROW("Spearow"),
	FEAROW("Fearow"),
	EKANS("Ekans"),
	ARBOK("Arbok"),
	PIKACHU("Pikachu"),
	RAICHU("Raichu"),
	SANDSHREW("Sandshrew"),
	SANDSLASH("Sandslash"),
	NIDORAN_F("Nidoran F"),
	NIDORINA("Nidorina"),
	NIDOQUEEN("Nidoqueen"),
	NIDORAN_M("Nidoran M"),
	NIDORINO("Nidorino"),
	NIDOKING("Nidoking"),
	CLEFAIRY("Clefairy"),
	CLEFABLE("Clefable"),
	VULPIX("Vulpix"),
	NINETALES("Ninetales"),
	JIGGLYPUFF("Jigglypuff"),
	WIGGLYTUFF("Wigglytuff"),
	ZUBAT("Zubat"),
	GOLBAT("Golbat"),
	ODDISH("Oddish"),
	GLOOM("Gloom"),
	VILEPLUME("Vileplume"),
	PARAS("Paras"),
	PARASECT("Parasect"),
	VENONAT("Venonat"),
	VENOMOTH("Venomoth"),
	DIGLETT("Diglett"),
	DUGTRIO("Dugtrio"),
	MEOWTH("Meowth"),
	PERSIAN("Persian"),
	PSYDUCK("Psyduck"),
	GOLDUCK("Golduck"),
	MANKEY("Mankey"),
	PRIMEAPE("Primeape"),
	GROWLITHE("Growlithe"),
	ARCANINE("Arcanine"),
	POLIWAG("Poliwag"),
	POLIWHIRL("Poliwhirl"),
	POLIWRATH("Poliwrath"),
	ABRA("Abra"),
	KADABRA("Kadabra"),
	ALAKAZAM("Alakazam"),
	MACHOP("Machop"),
	MACHOKE("Machoke"),
	MACHAMP("Machamp"),
	BELLSPROUT("Bellsprout"),
	WEEPINBELL("Weepinbell"),
	VICTREEBEL("Victreebel"),
	TENTACOOL("Tentacool"),
	TENTACRUEL("Tentacruel"),
	GEODUDE("Geodude"),
	GRAVELER("Graveler"),
	GOLEM("Golem"),
	PONYTA("Ponyta"),
	RAPIDASH("Rapidash"),
	SLOWPOKE("Slowpoke"),
	SLOWBRO("Slowbro"),
	MAGNEMITE("Magnemite"),
	MAGNETON("Magneton"),
	FARFETCHD("Farfetch'd"),
	DODUO("Doduo"),
	DODRIO("Dodrio"),
	SEEL("Seel"),
	DEWGONG("Dewgong"),
	GRIMER("Grimer"),
	MUK("Muk"),
	SHELLDER("Shellder"),
	CLOYSTER("Cloyster"),
	GASTLY("Gastly"),
	HAUNTER("Haunter"),
	GENGAR("Gengar"),
	ONIX("Onix"),
	DROWZEE("Drowzee"),
	HYPNO("Hypno"),
	KRABBY("Krabby"),
	KINGLER("Kingler"),
	VOLTORB("Voltorb"),
	ELECTRODE("Electrode"),
	EXEGGCUTE("Exeggcute"),
	EXEGGUTOR("Exeggutor"),
	CUBONE("Cubone"),
	MAROWAK("Marowak"),
	HITMONLEE("Hitmonlee"),
	HITMONCHAN("Hitmonchan"),
	LICKITUNG("Lickitung"),
	KOFFING("Koffing"),
	WEEZING("Weezing"),
	RHYHORN("Rhyhorn"),
	RHYDON("Rhydon"),
	CHANSEY("Chansey"),
	TANGELA("Tangela"),
	KANGASKHAN("Kangaskhan"),
	HORSEA("Horsea"),
	SEADRA("Seadra"),
	GOLDEEN("Goldeen"),
	SEAKING("Seaking"),
	STARYU("Staryu"),
	STARMIE("Starmie"),
	MR_MIME("Mr. Mime"),
	SCYTHER("Scyther"),
	JYNX("Jynx"),
	ELECTABUZZ("Electabuzz"),
	MAGMAR("Magmar"),
	PINSIR("Pinsir"),
	TAUROS("Tauros"),
	MAGIKARP("Magikarp"),
	GYARADOS("Gyarados"),
	LAPRAS("Lapras"),
	DITTO("Ditto"),
	EEVEE("Eevee"),
	VAPOREON("Vaporeon"),
	JOLTEON("Jolteon"),
	FLAREON("Flareon"),
	PORYGON("Porygon"),
	OMANYTE("Omanyte"),
	OMASTAR("Omastar"),
	KABUTO("Kabuto"),
	KABUTOPS("Kabutops"),
	AERODACTYL("Aerodactyl"),
	SNORLAX("Snorlax"),
	ARTICUNO("Articuno"),
	ZAPDOS("Zapdos"),
	MOLTRES("Moltres"),
	DRATINI("Dratini"),
	DRAGONAIR("Dragonair"),
	DRAGONITE("Dragonite"),
	MEWTWO("Mewtwo"),
	MEW("Mew"),
	CHIKORITA("Chikorita"),
	BAYLEEF("Bayleef"),
	MEGANIUM("Meganium"),
	CYNDAQUIL("Cyndaquil"),
	QUILAVA("Quilava"),
	TYPHLOSION("Typhlosion"),
	TOTODILE("Totodile"),
	CROCONAW("Croconaw"),
	FERALIGATR("Feraligatr"),
	SENTRET("Sentret"),
	FURRET("Furret"),
	HOOTHOOT("Hoothoot"),
	NOCTOWL("Noctowl"),
	LEDYBA("Ledyba"),
	LEDIAN("Ledian"),
	SPINARAK("Spinarak"),
	ARIADOS("Ariados"),
	CROBAT("Crobat"),
	CHINCHOU("Chinchou"),
	LANTURN("Lanturn"),
	PICHU("Pichu"),
	CLEFFA("Cleffa"),
	IGGLYBUFF("Igglybuff"),
	TOGEPI("Togepi"),
	TOGETIC("Togetic"),
	NATU("Natu"),
	XATU("Xatu"),
	MAREEP("Mareep"),
	FLAAFFY("Flaaffy"),
	AMPHAROS("Ampharos"),
	BELLOSSOM("Bellossom"),
	MARILL("Marill"),
	AZUMARILL("Azumarill"),
	SUDOWOODO("Sudowoodo"),
	POLITOED("Politoed"),
	HOPPIP("Hoppip"),
	SKIPLOOM("Skiploom"),
	JUMPLUFF("Jumpluff"),
	AIPOM("Aipom"),
	SUNKERN("Sunkern"),
	SUNFLORA("Sunflora"),
	YANMA("Yanma"),
	WOOPER("Wooper"),
	QUAGSIRE("Quagsire"),
	ESPEON("Espeon"),
	UMBREON("Umbreon"),
	MURKROW("Murkrow"),
	SLOWKING("Slowking"),
	MISDREAVUS("Misdreavus"),
	UNOWN("Unown"),
	WOBBUFFET("Wobbuffet"),
	GIRAFARIG("Girafarig"),
	PINECO("Pineco"),
	FORRETRESS("Forretress"),
	DUNSPARCE("Dunsparce"),
	GLIGAR("Gligar"),
	STEELIX("Steelix"),
	SNUBBULL("Snubbull"),
	GRANBULL("Granbull"),
	QWILFISH("Qwilfish"),
	SCIZOR("Scizor"),
	SHUCKLE("Shuckle"),
	HERACROSS("Heracross"),
	SNEASEL("Sneasel"),
	TEDDIURSA("Teddiursa"),
	URSARING("Ursaring"),
	SLUGMA("Slugma"),
	MAGCARGO("Magcargo"),
	SWINUB("Swinub"),
	PILOSWINE("Piloswine"),
	CORSOLA("Corsola"),
	REMORAID("Remoraid"),
	OCTILLERY("Octillery"),
	DELIBIRD("Delibird"),
	MANTINE("Mantine"),
	SKARMORY("Skarmory"),
	HOUNDOUR("Houndour"),
	HOUNDOOM("Houndoom"),
	KINGDRA("Kingdra"),
	PHANPY("Phanpy"),
	DONPHAN("Donphan"),
	PORYGON2("Porygon2"),
	STANTLER("Stantler"),
	SMEARGLE("Smeargle"),
	TYROGUE("Tyrogue"),
	HITMONTOP("Hitmontop"),
	SMOOCHUM("Smoochum"),
	ELEKID("Elekid"),
	MAGBY("Magby"),
	MILTANK("Miltank"),
	BLISSEY("Blissey"),
	RAIKOU("Raikou"),
	ENTEI("Entei"),
	SUICUNE("Suicune"),
	LARVITAR("Larvitar"),
	PUPITAR("Pupitar"),
	TYRANITAR("Tyranitar"),
	LUGIA("Lugia"),
	HO_OH("Ho-Oh"),
	CELEBI("Celebi"),
	TREECKO("Treecko"),
	GROVYLE("Grovyle"),
	SCEPTILE("Sceptile"),
	TORCHIC("Torchic"),
	COMBUSKEN("Combusken"),
	BLAZIKEN("Blaziken"),
	MUDKIP("Mudkip"),
	MARSHTOMP("Marshtomp"),
	SWAMPERT("Swampert"),
	POOCHYENA("Poochyena"),
	MIGHTYENA("Mightyena"),
	ZIGZAGOON("Zigzagoon"),
	LINOONE("Linoone"),
	WURMPLE("Wurmple"),
	SILCOON("Silcoon"),
	BEAUTIFLY("Beautifly"),
	CASCOON("Cascoon"),
	DUSTOX("Dustox"),
	LOTAD("Lotad"),
	LOMBRE("Lombre"),
	LUDICOLO("Ludicolo"),
	SEEDOT("Seedot"),
	NUZLEAF("Nuzleaf"),
	SHIFTRY("Shiftry"),
	TAILLOW("Taillow"),
	SWELLOW("Swellow"),
	WINGULL("Wingull"),
	PELIPPER("Pelipper"),
	RALTS("Ralts"),
	KIRLIA("Kirlia"),
	GARDEVOIR("Gardevoir"),
	SURSKIT("Surskit"),
	MASQUERAIN("Masquerain"),
	SHROOMISH("Shroomish"),
	BRELOOM("Breloom"),
	SLAKOTH("Slakoth"),
	VIGOROTH("Vigoroth"),
	SLAKING("Slaking"),
	NINCADA("Nincada"),
	NINJASK("Ninjask"),
	SHEDINJA("Shedinja"),
	WHISMUR("Whismur"),
	LOUDRED("Loudred"),
	EXPLOUD("Exploud"),
	MAKUHITA("Makuhita"),
	HARIYAMA("Hariyama"),
	AZURILL("Azurill"),
	NOSEPASS("Nosepass"),
	SKITTY("Skitty"),
	DELCATTY("Delcatty"),
	SABLEYE("Sableye"),
	MAWILE("Mawile"),
	ARON("Aron"),
	LAIRON("Lairon"),
	AGGRON("Aggron"),
	MEDITITE("Meditite"),
	MEDICHAM("Medicham"),
	ELECTRIKE("Electrike"),
	MANECTRIC("Manectric"),
	PLUSLE("Plusle"),
	MINUN("Minun"),
	VOLBEAT("Volbeat"),
	ILLUMISE("Illumise"),
	ROSELIA("Roselia"),
	GULPIN("Gulpin"),
	SWALOT("Swalot"),
	CARVANHA("Carvanha"),
	SHARPEDO("Sharpedo"),
	WAILMER("Wailmer"),
	WAILORD("Wailord"),
	NUMEL("Numel"),
	CAMERUPT("Camerupt"),
	TORKOAL("Torkoal"),
	SPOINK("Spoink"),
	GRUMPIG("Grumpig"),
	SPINDA("Spinda"),
	TRAPINCH("Trapinch"),
	VIBRAVA("Vibrava"),
	FLYGON("Flygon"),
	CACNEA("Cacnea"),
	CACTURNE("Cacturne"),
	SWABLU("Swablu"),
	ALTARIA("Altaria"),
	ZANGOOSE("Zangoose"),
	SEVIPER("Seviper"),
	LUNATONE("Lunatone"),
	SOLROCK("Solrock"),
	BARBOACH("Barboach"),
	WHISCASH("Whiscash"),
	CORPHISH("Corphish"),
	CRAWDAUNT("Crawdaunt"),
	BALTOY("Baltoy"),
	CLAYDOL("Claydol"),
	LILEEP("Lileep"),
	CRADILY("Cradily"),
	ANORITH("Anorith"),
	ARMALDO("Armaldo"),
	FEEBAS("Feebas"),
	MILOTIC("Milotic"),
	CASTFORM("Castform"),
	KECLEON("Kecleon"),
	SHUPPET("Shuppet"),
	BANETTE("Banette"),
	DUSKULL("Duskull"),
	DUSCLOPS("Dusclops"),
	TROPIUS("Tropius"),
	CHIMECHO("Chimecho"),
	ABSOL("Absol"),
	WYNAUT("Wynaut"),
	SNORUNT("Snorunt"),
	GLALIE("Glalie"),
	SPHEAL("Spheal"),
	SEALEO("Sealeo"),
	WALREIN("Walrein"),
	CLAMPERL("Clamperl"),
	HUNTAIL("Huntail"),
	GOREBYSS("Gorebyss"),
	RELICANTH("Relicanth"),
	LUVDISC("Luvdisc"),
	BAGON("Bagon"),
	SHELGON("Shelgon"),
	SALAMENCE("Salamence"),
	BELDUM("Beldum"),
	METANG("Metang"),
	METAGROSS("Metagross"),
	REGIROCK("Regirock"),
	REGICE("Regice"),
	REGISTEEL("Registeel"),
	LATIAS("Latias"),
	LATIOS("Latios"),
	KYOGRE("Kyogre"),
	GROUDON("Groudon"),
	RAYQUAZA("Rayquaza"),
	JIRACHI("Jirachi"),
	DEOXYS("Deoxys"),
	TURTWIG("Turtwig"),
	GROTLE("Grotle"),
	TORTERRA("Torterra"),
	CHIMCHAR("Chimchar"),
	MONFERNO("Monferno"),
	INFERNAPE("Infernape"),
	PIPLUP("Piplup"),
	PRINPLUP("Prinplup"),
	EMPOLEON("Empoleon"),
	STARLY("Starly"),
	STARAVIA("Staravia"),
	STARAPTOR("Staraptor"),
	BIDOOF("Bidoof"),
	BIBAREL("Bibarel"),
	KRICKETOT("Kricketot"),
	KRICKETUNE("Kricketune"),
	SHINX("Shinx"),
	LUXIO("Luxio"),
	LUXRAY("Luxray"),
	BUDEW("Budew"),
	ROSERADE("Roserade"),
	CRANIDOS("Cranidos"),
	RAMPARDOS("Rampardos"),
	SHIELDON("Shieldon"),
	BASTIODON("Bastiodon"),
	BURMY("Burmy"),
	WORMADAM("Wormadam"),
	MOTHIM("Mothim"),
	COMBEE("Combee"),
	VESPIQUEN("Vespiquen"),
	PACHIRISU("Pachirisu"),
	BUIZEL("Buizel"),
	FLOATZEL("Floatzel"),
	CHERUBI("Cherubi"),
	CHERRIM("Cherrim"),
	SHELLOS("Shellos"),
	GASTRODON("Gastrodon"),
	AMBIPOM("Ambipom"),
	DRIFLOON("Drifloon"),
	DRIFBLIM("Drifblim"),
	BUNEARY("Buneary"),
	LOPUNNY("Lopunny"),
	MISMAGIUS("Mismagius"),
	HONCHKROW("Honchkrow"),
	GLAMEOW("Glameow"),
	PURUGLY("Purugly"),
	CHINGLING("Chingling"),
	STUNKY("Stunky"),
	SKUNTANK("Skuntank"),
	BRONZOR("Bronzor"),
	BRONZONG("Bronzong"),
	BONSLY("Bonsly"),
	MIME_JR("Mime Jr."),
	HAPPINY("Happiny"),
	CHATOT("Chatot"),
	SPIRITOMB("Spiritomb"),
	GIBLE("Gible"),
	GABITE("Gabite"),
	GARCHOMP("Garchomp"),
	MUNCHLAX("Munchlax"),
	RIOLU("Riolu"),
	LUCARIO("Lucario"),
	HIPPOPOTAS("Hippopotas"),
	HIPPOWDON("Hippowdon"),
	SKORUPI("Skorupi"),
	DRAPION("Drapion"),
	CROAGUNK("Croagunk"),
	TOXICROAK("Toxicroak"),
	CARNIVINE("Carnivine"),
	FINNEON("Finneon"),
	LUMINEON("Lumineon"),
	MANTYKE("Mantyke"),
	SNOVER("Snover"),
	ABOMASNOW("Abomasnow"),
	WEAVILE("Weavile"),
	MAGNEZONE("Magnezone"),
	LICKILICKY("Lickilicky"),
	RHYPERIOR("Rhyperior"),
	TANGROWTH("Tangrowth"),
	ELECTIVIRE("Electivire"),
	MAGMORTAR("Magmortar"),
	TOGEKISS("Togekiss"),
	YANMEGA("Yanmega"),
	LEAFEON("Leafeon"),
	GLACEON("Glaceon"),
	GLISCOR("Gliscor"),
	MAMOSWINE("Mamoswine"),
	PORYGON_Z("Porygon-Z"),
	GALLADE("Gallade"),
	PROBOPASS("Probopass"),
	DUSKNOIR("Dusknoir"),
	FROSLASS("Froslass"),
	ROTOM("Rotom"),
	UXIE("Uxie"),
	MESPRIT("Mesprit"),
	AZELF("Azelf"),
	DIALGA("Dialga"),
	PALKIA("Palkia"),
	HEATRAN("Heatran"),
	REGIGIGAS("Regigigas"),
	GIRATINA("Giratina"),
	CRESSELIA("Cresselia"),
	PHIONE("Phione"),
	MANAPHY("Manaphy"),
	DARKRAI("Darkrai"),
	SHAYMIN("Shaymin"),
	ARCEUS("Arceus"),
	VICTINI("Victini"),
	SNIVY("Snivy"),
	SERVINE("Servine"),
	SERPERIOR("Serperior"),
	TEPIG("Tepig"),
	PIGNITE("Pignite"),
	EMBOAR("Emboar"),
	OSHAWOTT("Oshawott"),
	DEWOTT("Dewott"),
	SAMUROTT("Samurott"),
	PATRAT("Patrat"),
	WATCHOG("Watchog"),
	LILLIPUP("Lillipup"),
	HERDIER("Herdier"),
	STOUTLAND("Stoutland"),
	PURRLOIN("Purrloin"),
	LIEPARD("Liepard"),
	PANSAGE("Pansage"),
	SIMISAGE("Simisage"),
	PANSEAR("Pansear"),
	SIMISEAR("Simisear"),
	PANPOUR("Panpour"),
	SIMIPOUR("Simipour"),
	MUNNA("Munna"),
	MUSHARNA("Musharna"),
	PIDOVE("Pidove"),
	TRANQUILL("Tranquill"),
	UNFEZANT("Unfezant"),
	BLITZLE("Blitzle"),
	ZEBSTRIKA("Zebstrika"),
	ROGGENROLA("Roggenrola"),
	BOLDORE("Boldore"),
	GIGALITH("Gigalith"),
	WOOBAT("Woobat"),
	SWOOBAT("Swoobat"),
	DRILBUR("Drilbur"),
	EXCADRILL("Excadrill"),
	AUDINO("Audino"),
	TIMBURR("Timburr"),
	GURDURR("Gurdurr"),
	CONKELDURR("Conkeldurr"),
	TYMPOLE("Tympole"),
	PALPITOAD("Palpitoad"),
	SEISMITOAD("Seismitoad"),
	THROH("Throh"),
	SAWK("Sawk"),
	SEWADDLE("Sewaddle"),
	SWADLOON("Swadloon"),
	LEAVANNY("Leavanny"),
	VENIPEDE("Venipede"),
	WHIRLIPEDE("Whirlipede"),
	SCOLIPEDE("Scolipede"),
	COTTONEE("Cottonee"),
	WHIMSICOTT("Whimsicott"),
	PETILIL("Petilil"),
	LILLIGANT("Lilligant"),
	BASCULIN("Basculin"),
	SANDILE("Sandile"),
	KROKOROK("Krokorok"),
	KROOKODILE("Krookodile"),
	DARUMAKA("Darumaka"),
	DARMANITAN("Darmanitan"),
	MARACTUS("Maractus"),
	DWEBBLE("Dwebble"),
	CRUSTLE("Crustle"),
	SCRAGGY("Scraggy"),
	SCRAFTY("Scrafty"),
	SIGILYPH("Sigilyph"),
	YAMASK("Yamask"),
	COFAGRIGUS("Cofagrigus"),
	TIRTOUGA("Tirtouga"),
	CARRACOSTA("Carracosta"),
	ARCHEN("Archen"),
	ARCHEOPS("Archeops"),
	TRUBBISH("Trubbish"),
	GARBODOR("Garbodor"),
	ZORUA("Zorua"),
	ZOROARK("Zoroark"),
	MINCCINO("Minccino"),
	CINCCINO("Cinccino"),
	GOTHITA("Gothita"),
	GOTHORITA("Gothorita"),
	GOTHITELLE("Gothitelle"),
	SOLOSIS("Solosis"),
	DUOSION("Duosion"),
	REUNICLUS("Reuniclus"),
	DUCKLETT("Ducklett"),
	SWANNA("Swanna"),
	VANILLITE("Vanillite"),
	VANILLISH("Vanillish"),
	VANILLUXE("Vanilluxe"),
	DEERLING("Deerling"),
	SAWSBUCK("Sawsbuck"),
	EMOLGA("Emolga"),
	KARRABLAST("Karrablast"),
	ESCAVALIER("Escavalier"),
	FOONGUS("Foongus"),
	AMOONGUSS("Amoonguss"),
	FRILLISH("Frillish"),
	JELLICENT("Jellicent"),
	ALOMOMOLA("Alomomola"),
	JOLTIK("Joltik"),
	GALVANTULA("Galvantula"),
	FERROSEED("Ferroseed"),
	FERROTHORN("Ferrothorn"),
	KLINK("Klink"),
	KLANG("Klang"),
	KLINKLANG("Klinklang"),
	TYNAMO("Tynamo"),
	EELEKTRIK("Eelektrik"),
	EELEKTROSS("Eelektross"),
	ELGYEM("Elgyem"),
	BEHEEYEM("Beheeyem"),
	LITWICK("Litwick"),
	LAMPENT("Lampent"),
	CHANDELURE("Chandelure"),
	AXEW("Axew"),
	FRAXURE("Fraxure"),
	HAXORUS("Haxorus"),
	CUBCHOO("Cubchoo"),
	BEARTIC("Beartic"),
	CRYOGONAL("Cryogonal"),
	SHELMET("Shelmet"),
	ACCELGOR("Accelgor"),
	STUNFISK("Stunfisk"),
	MIENFOO("Mienfoo"),
	MIENSHAO("Mienshao"),
	DRUDDIGON("Druddigon"),
	GOLETT("Golett"),
	GOLURK("Golurk"),
	PAWNIARD("Pawniard"),
	BISHARP("Bisharp"),
	BOUFFALANT("Bouffalant"),
	RUFFLET("Rufflet"),
	BRAVIARY("Braviary"),
	VULLABY("Vullaby"),
	MANDIBUZZ("Mandibuzz"),
	HEATMOR("Heatmor"),
	DURANT("Durant"),
	DEINO("Deino"),
	ZWEILOUS("Zweilous"),
	HYDREIGON("Hydreigon"),
	LARVESTA("Larvesta"),
	VOLCARONA("Volcarona"),
	COBALION("Cobalion"),
	TERRAKION("Terrakion"),
	VIRIZION("Virizion"),
	TORNADUS("Tornadus"),
	THUNDURUS("Thundurus"),
	RESHIRAM("Reshiram"),
	ZEKROM("Zekrom"),
	LANDORUS("Landorus"),
	KYUREM("Kyurem"),
	KELDEO("Keldeo"),
	MELOETTA("Meloetta"),
	GENESECT("Genesect"),
	CHESPIN("Chespin"),
	QUILLADIN("Quilladin"),
	CHESNAUGHT("Chesnaught"),
	FENNEKIN("Fennekin"),
	BRAIXEN("Braixen"),
	DELPHOX("Delphox"),
	FROAKIE("Froakie"),
	FROGADIER("Frogadier"),
	GRENINJA("Greninja"),
	BUNNELBY("Bunnelby"),
	DIGGERSBY("Diggersby"),
	FLETCHLING("Fletchling"),
	FLETCHINDER("Fletchinder"),
	TALONFLAME("Talonflame"),
	SCATTERBUG("Scatterbug"),
	SPEWPA("Spewpa"),
	VIVILLON("Vivillon"),
	LITLEO("Litleo"),
	PYROAR("Pyroar"),
	FLABEBE("Flabebe"),
	FLOETTE("Floette"),
	FLORGES("Florges"),
	SKIDDO("Skiddo"),
	GOGOAT("Gogoat"),
	PANCHAM("Pancham"),
	PANGORO("Pangoro"),
	FURFROU("Furfrou"),
	ESPURR("Espurr"),
	MEOWSTIC("Meowstic"),
	HONEDGE("Honedge"),
	DOUBLADE("Doublade"),
	AEGISLASH("Aegislash"),
	SPRITZEE("Spritzee"),
	AROMATISSE("Aromatisse"),
	SWIRLIX("Swirlix"),
	SLURPUFF("Slurpuff"),
	INKAY("Inkay"),
	MALAMAR("Malamar"),
	BINACLE("Binacle"),
	BARBARACLE("Barbaracle"),
	SKRELP("Skrelp"),
	DRAGALGE("Dragalge"),
	CLAUNCHER("Clauncher"),
	CLAWITZER("Clawitzer"),
	HELIOPTILE("Helioptile"),
	HELIOLISK("Heliolisk"),
	TYRUNT("Tyrunt"),
	TYRANTRUM("Tyrantrum"),
	AMAURA("Amaura"),
	AURORUS("Aurorus"),
	SYLVEON("Sylveon"),
	HAWLUCHA("Hawlucha"),
	DEDENNE("Dedenne"),
	CARBINK("Carbink"),
	GOOMY("Goomy"),
	SLIGGOO("Sliggoo"),
	GOODRA("Goodra"),
	KLEFKI("Klefki"),
	PHANTUMP("Phantump"),
	TREVENANT("Trevenant"),
	PUMPKABOO("Pumpkaboo"),
	GOURGEIST("Gourgeist"),
	BERGMITE("Bergmite"),
	AVALUGG("Avalugg"),
	NOIBAT("Noibat"),
	NOIVERN("Noivern"),
	XERNEAS("Xerneas"),
	YVELTAL("Yveltal"),
	ZYGARDE("Zygarde"),
	DIANCIE("Diancie"),
	HOOPA("Hoopa"),
	VOLCANION("Volcanion"),
	ROWLET("Rowlet"),
	DARTRIX("Dartrix"),
	DECIDUEYE("Decidueye"),
	LITTEN("Litten"),
	TORRACAT("Torracat"),
	INCINEROAR("Incineroar"),
	POPPLIO("Popplio"),
	BRIONNE("Brionne"),
	PRIMARINA("Primarina"),
	PIKIPEK("Pikipek"),
	TRUMBEAK("Trumbeak"),
	TOUCANNON("Toucannon"),
	YUNGOOS("Yungoos"),
	GUMSHOOS("Gumshoos"),
	GRUBBIN("Grubbin"),
	CHARJABUG("Charjabug"),
	VIKAVOLT("Vikavolt"),
	CRABRAWLER("Crabrawler"),
	CRABOMINABLE("Crabominable"),
	ORICORIO("Oricorio"),
	CUTIEFLY("Cutiefly"),
	RIBOMBEE("Ribombee"),
	ROCKRUFF("Rockruff"),
	LYCANROC("Lycanroc"),
	WISHIWASHI("Wishiwashi"),
	MAREANIE("Mareanie"),
	TOXAPEX("Toxapex"),
	MUDBRAY("Mudbray"),
	MUDSDALE("Mudsdale"),
	DEWPIDER("Dewpider"),
	ARAQUANID("Araquanid"),
	FOMANTIS("Fomantis"),
	LURANTIS("Lurantis"),
	MORELULL("Morelull"),
	SHIINOTIC("Shiinotic"),
	SALANDIT("Salandit"),
	SALAZZLE("Salazzle"),
	STUFFUL("Stufful"),
	BEWEAR("Bewear"),
	BOUNSWEET("Bounsweet"),
	STEENEE("Steenee"),
	TSAREENA("Tsareena"),
	COMFEY("Comfey"),
	ORANGURU("Oranguru"),
	PASSIMIAN("Passimian"),
	WIMPOD("Wimpod"),
	GOLISOPOD("Golisopod"),
	SANDYGAST("Sandygast"),
	PALOSSAND("Palossand"),
	PYUKUMUKU("Pyukumuku"),
	TYPE_NULL("Type: Null"),
	SILVALLY("Silvally"),
	MINIOR("Minior"),
	KOMALA("Komala"),
	TURTONATOR("Turtonator"),
	TOGEDEMARU("Togedemaru"),
	MIMIKYU("Mimikyu"),
	BRUXISH("Bruxish"),
	DRAMPA("Drampa"),
	DHELMISE("Dhelmise"),
	JANGMO_O("Jangmo-o"),
	HAKAMO_O("Hakamo-o"),
	KOMMO_O("Kommo-o"),
	TAPU_KOKO("Tapu Koko"),
	TAPU_LELE("Tapu Lele"),
	TAPU_BULU("Tapu Bulu"),
	TAPU_FINI("Tapu Fini"),
	COSMOG("Cosmog"),
	COSMOEM("Cosmoem"),
	SOLGALEO("Solgaleo"),
	LUNALA("Lunala"),
	NIHILEGO("Nihilego"),
	BUZZWOLE("Buzzwole"),
	PHEROMOSA("Pheromosa"),
	XURKITREE("Xurkitree"),
	CELESTEELA("Celesteela"),
	KARTANA("Kartana"),
	GUZZLORD("Guzzlord"),
	NECROZMA("Necrozma"),
	MAGEARNA("Magearna"),
	MARSHADOW("Marshadow"),
	RIZARDON("Rizardon"),
	KUCHIITO("Kuchiito"),
	ASBEL("Asbel"),
	YAMIRAMI("Yamirami"),
	SILPH_SURFER("Silph Surfer"),
	SNOWSHREW("Snowshrew"),
	SNOWSLASH("Snowslash"),
	YUKIKON("Yukikon"),
	KYUKON("Kyukon"),
	SLEIMA("Sleima"),
	SLEIMOK("Sleimok"),
	KOKONATSU("Kokonatsu"),
	GARA_GARA("GaraGara"),
	JUPETTA("Jupetta"),
	LOUGAROC("Lougaroc");

    // EVERYTHING ABOVE IS GENERATED ###

    private final String name;

    PokemonNamesies(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public static PokemonNamesies tryValueOf(String name) {
		try {
			return PokemonNamesies.valueOf(PokeString.getNamesiesString(name));
		} catch (IllegalArgumentException exception) {
			return null;
		}
	}

    public static PokemonNamesies getValueOf(String name) {
        PokemonNamesies namesies = tryValueOf(name);
		if (namesies == null) {
			Global.error(name + " does not have a valid PokemonNamesies value");
		}

		return namesies;
    }
}

