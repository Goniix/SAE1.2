import extensions.CSVFile;
import extensions.File;
import ijava.Curses;
class Splask extends Program{
    final String ASCIILINE = "-----------------------------------------------";
    final String DECK_FILE = "ressources/deckList.txt";
    final String MONTERS_STATS = "ressources/monsterStats.csv";

    final String[] MONSTER_NAMES_LIST = new String [] {"DOG","WOLF","GHOST","GOOE","TOXIC_WRAITH","SKULL","DOOR_SKULL","WRAITH","ZOMBIE","BB_DRAGON","LICH","WEREWOLF","DRAGON","DRAGON_SEER","DRAGON_LORD"};

    final int BUFFID_SHIELD = 0;
    final int BUFFID_BLEED = 1;
    final int BUFFID_POISON = 2;
    final int BUFFID_SHOCK = 3;
    final int BUFFID_CONCUSS = 4;
    final int BUFFID_IGNITE = 5;
    final int BUFFID_REGEN = 6;
    final int BUFFID_RIGHTNESS = 7;
    
    //GENERAL METHODS--------------------------------------------------------------------------------------------
    int clamp(int val, int min, int max){
        return Math.max(min,Math.min(max,val));
    }

    int lineCount(String fileName){
        File importedFile = newFile(fileName);

        String line = readLine(importedFile);

        int res = 0;
        while(line != null){
            line = readLine(importedFile);
            res++;
        }
        return res;
    }

    int max(int numb1, int numb2){
        return (numb1>numb2) ? numb1 : numb2;
    }

    boolean isStringNumeric(String chain){
        boolean res = true;
        int index = 0;
        while(index<length(chain) && res){
            char elem = chain.charAt(index);
            if(elem<'0' || elem>'9') res = false;
            index++;
        }
        return res;
    }

    int getPlayerInput(int max){
        String input;
        String stringedMax = max + "";
        int res;
        do{
            do{
                print("Entrez un chiffre entre 1 et "+max+": ");
                input=readString();
            }while(length(input)>length(stringedMax) || length(input)==0 || !isStringNumeric(input));
            res = Integer.parseInt(input);
        }while(res>max || res<1);
        return res-1;
    }

    String[] shuffle(String[] list){
        int len = length(list);
        for(int index = 0; index<len; index++){
            int randomIndex = (int) (random()*len);
            String temp = list[index];
            list[index] = list[randomIndex];
            list[randomIndex] = temp;
        }
        return list;
    }

    void println(String[] list){
        String res = "[";
        for(int index = 0; index<length(list); index++){
            res+=list[index]+", ";
        }
        println(res+"]");
    }


    
    //DYNAMIC PILES METHODS
    //INT PILES
    int[] rebuild(int[] pile, int len){
        int[] newPile = new int[len];
        int refIndex = 0;
        int oldIndex = 0;
        while(refIndex<len && oldIndex<length(pile)){
            if(pile[oldIndex] != -1){
                newPile[refIndex] = pile[oldIndex];
                refIndex++;
            }
            oldIndex++;
        }
        return newPile;
    }

    int[] append(int[] pile, int element){
        int[] res = rebuild(pile,length(pile)+1);
        res[length(pile)] = element;
        return res;
    }

    int[] append(int[] pile, int[] element){
        int[] res = rebuild(pile,length(pile)+length(element));
        for(int index = length(pile); index<length(pile)+length(element); index++){
            res[index] = element[index-length(pile)];
        }
        return res;
    }

    int[] remove(int[] pile, int removeIndex){
        int[] res = new int[length(pile)-1];
        for(int pileIndex = 0; pileIndex<length(pile)-1; pileIndex++){
            if(pileIndex>=removeIndex) res[pileIndex] = pile[pileIndex+1];
            else res[pileIndex] = pile[pileIndex];
        }
        return res;
    }

    int[] shuffle(int[] pile){
        int len = length(pile);
        for(int index = 0; index<len; index++){
            int randomIndex = (int) (random()*len);
            int temp = pile[index];
            pile[index] = pile[randomIndex];
            pile[randomIndex] = temp;
        }
        return pile;
    }

    int[] copy(int[] pile){
        int[] res = new int[length(pile)];
        for(int index = 0; index<length(pile); index++){
            res[index] = pile[index];
        }
        return res;
    }

    void println(int[] pile){
        String res = "[";
        for(int index = 0; index<length(pile); index++){
            res+=pile[index]+", ";
        }
        println(res+"]");
    }

    void switchGameState(GameState state, Game game){
        game.gameState = state;
        game.initGameState = true;
        clearScreen();
    }

    //ABILITY METHODS--------------------------------------------------------------------------------------------
    Ability newAbility(String effect, int power, String target){
        //converts string loaded data from spellList into ability class objects
        Ability res = new Ability();
        Effect type;
        switch(effect){
            case "DMG":
                type = Effect.DAMAGE;
                break;
            case "HEL":
                type = Effect.HEAL;
                break;
            case "SHD":
                type = Effect.SHIELD;
                break;
            case "BLD":
                type = Effect.BLEED;
                break;
            case "PSN":
                type = Effect.POISON;
                break;
            case "SHK":
                type = Effect.SHOCK;
                break;
            case "CNC":
                type = Effect.CONCUSS;
                break;
            case "IGN":
                type = Effect.IGNITE;
                break;
            case "RGN":
                type = Effect.REGEN;
                break;
            default:
                type = null;
                throw new RuntimeException("Effect \""+effect+"\" was not found");
        }
        res.effectType = type;
        res.power = power;
        
        Target targ;
        switch(target){
            case "SLF":
                targ = Target.SELF;
                break;
            case "FOE":
                targ = Target.FOE;
                break; 
            default:
                targ = null;
                throw new RuntimeException("Target \""+target+"\" was not found");
        }
        res.targetType = targ;
        return res; 
    }

    String toString(Ability ability){
        String res = "";
        res+= ability.power+" ";
        res+= toString(ability.effectType);
        res+=" à ";
        res+= toString(ability.targetType);

        return res;
    }

    void executeAbility(Ability ability, Unit targetUnit, double multiplicator){
        int basePower = (int) (ability.power * multiplicator);
        int power = basePower;

        Effect type = ability.effectType;
        switch(type){
            case DAMAGE:
                if(targetUnit.buffList[BUFFID_CONCUSS] != null){
                    //is concussed
                    power *= 1.3;
                    basePower *= 1.3;
                }
                if(targetUnit.buffList[BUFFID_RIGHTNESS] != null){
                    power *= 0.7;
                    basePower *= 0.7;
                }

                if(targetUnit.buffList[BUFFID_SHIELD] != null){
                    //is shielded
                    power = max(0, power-targetUnit.buffList[BUFFID_SHIELD].power);
                    targetUnit.buffList[BUFFID_SHIELD].power -= basePower-power;
                }
                targetUnit.health -= power;

                

                println(targetUnit.name+" subit "+basePower+" dégats");

                if(targetUnit.buffList[BUFFID_BLEED] != null){
                    //is bleeding
                    targetUnit.buffList[BUFFID_BLEED].power += power/3;
                    println(targetUnit.name+" saigne de plus en plus!");
                }

                break;

            case HEAL:
                if(targetUnit.buffList[BUFFID_IGNITE]==null){
                    //not burning
                    if(targetUnit.buffList[BUFFID_BLEED]!=null){
                        //is bleeding
                        if(targetUnit.buffList[BUFFID_BLEED].power == 0){
                            //no damages stored in bleed
                            targetUnit.buffList[BUFFID_BLEED] = null;
                            println(targetUnit.name+" parvient à stopper le saignement!");
                        }
                        else{
                            //some damages are stored in bleed
                            targetUnit.buffList[BUFFID_BLEED].power = max(0,targetUnit.buffList[BUFFID_BLEED].power-power);
                            println(targetUnit.name+" réduit le saignement de "+power+"!");
                        }
                        
                    }
                    //targetUnit.health = clamp(targetUnit.health+power,0,targetUnit.maxHealth);
                    healDamage(targetUnit,power);
                    println(targetUnit.name+" se soigne de "+power+" HP");

                }
                else{
                    //is burning
                    println(targetUnit.name+" brule! Il ne parvient pas à refermer ses plaies!");
                }

                break;

            case SHIELD:
                if(targetUnit.buffList[BUFFID_SHIELD] == null){
                    targetUnit.buffList[BUFFID_SHIELD] = newBuff(1,power,type);
                }
                else{
                    targetUnit.buffList[BUFFID_SHIELD].power += power;
                }
                println(targetUnit.name+" se protège de "+power);
                break;

            case BLEED:
                if(targetUnit.buffList[BUFFID_BLEED] == null){
                    targetUnit.buffList[BUFFID_BLEED] = newBuff(power,0,type);
                    println(targetUnit.name+" se met à saigner pour "+power+" tours!");
                }
                else{
                    targetUnit.buffList[BUFFID_BLEED].duration += 2;
                    println(targetUnit.name+" saigne plus longtemps (encore"+targetUnit.buffList[BUFFID_BLEED].duration+" tours)!");
                }
                break;

            case POISON:
                targetUnit.buffList[BUFFID_POISON] = newBuff(3,power,type);
                println(targetUnit.name+" est empoisonné! Il perd "+(power*10)+"% de vie par tour pour 3 tours!");
                break;
            
            case SHOCK:
                if(targetUnit.buffList[BUFFID_SHOCK] == null){
                    targetUnit.buffList[BUFFID_SHOCK] = newBuff(2,power,type);
                }
                else{
                    targetUnit.buffList[BUFFID_SHOCK].power += power;
                }
                println(targetUnit.name+" subira "+targetUnit.buffList[BUFFID_SHOCK].power+" dégats au début de son prochain tour!");
                break;
            
            case CONCUSS:
                targetUnit.buffList[BUFFID_CONCUSS] = newBuff(power,3,type);
                println(targetUnit.name+" est étourdit! Il subit 30% de dégats en plus pour 3 tours!");
                break;
            
            case IGNITE:
                targetUnit.buffList[BUFFID_IGNITE] = newBuff(power,0,type);
                println(targetUnit.name + " brûle! Il ne peut plus se soigner pendant "+power+" tours!");
                break;
            
            case REGEN:
                targetUnit.buffList[BUFFID_REGEN] = newBuff(3,power,type);
                println(targetUnit.name + " commence à se régénérer de "+power+" PV pendant 3 tours!");
                break;

                
        }
    }

    //UNIT METHODS--------------------------------------------------------------------------------------------
    Unit newUnit(String name){
        Unit res = new Unit();
        res.name = name;
        res.maxHealth = 20;
        res.health = res.maxHealth;
        res.shield = 0;
        res.strength = 1.0;
        res.hand = new int[] {-1,-1,-1,-1};
        res.discard = new int[0];
        return res;
    }

    String toString(Unit unit){
        String res = ASCIILINE+"\n";
        res+= "Nom: "+ unit.name +"\n";
        res+= "Vie: "+ unit.health;
        if(unit.buffList[BUFFID_SHIELD]!=null) res += " + " + unit.buffList[BUFFID_SHIELD].power; 
        res+= " / " + unit.maxHealth + "\n";
        final int buffCount = length(unit.buffList);
        for(int index = 0; index<buffCount; index++){
            Buff elem = unit.buffList[index];
            if(elem!=null){
                res+=toString(elem)+"\n";
            }
        }
        res+= ASCIILINE;
        return res;
    }

    String[] readDeckFile(String fileName, String deckID){
    //imports deck list from file into a list of String (named deckList)
        File importedFile = newFile(fileName);

        String line;
        do{
            line = readLine(importedFile);
        }while(line != null && !line.equals(deckID));

        // int deckSize = (line!=null) ? Integer.parseInt(readLine(importedFile)) : 0;
        int deckSize = (line!=null) ? getDeckSpellCount(fileName, deckID) : 0;
        String[] res = new String[deckSize];

        for(int index = 0; index<deckSize; index++){
            res[index] = readLine(importedFile);
        }

        return res;
    }

    int[] readDeckList(String[] stringedList, SpellBook book){
    //imports spell id from list of String (deckList)
        int stringedLen = length(stringedList);
        int[] res = new int[stringedLen];
        println(stringedList);
        for(int index = 0; index<stringedLen; index++){
            res[index] = getSpellIndex(book,stringedList[index]);
        }
        return res;
    }

    void importDeck(Unit unit, String deckFileName, SpellBook book){
        unit.baseDeck = readDeckList(readDeckFile(deckFileName,unit.name),book);
        unit.deck = copy(unit.baseDeck);
    }

    void drawCard(Unit unit, int count){
        for(int index = 0; index<count; index++){
            if (length(unit.deck) == 0) remakeDeck(unit);
            unit.hand = append(unit.hand, unit.deck[length(unit.deck)-1]);
            unit.deck = rebuild(unit.deck, length(unit.deck)-1);

            //if(unit.name.equals("PLAYER")){
            int drawedSpellIndex = unit.hand[length(unit.hand)-1];
            String drawedSpellName = unit.gameLink.theBook.allSpells[drawedSpellIndex].name;
            println(unit.name+" pioche "+drawedSpellName);
            // }
        }
    }

    void resetDeck(Unit unit){
        unit.deck = copy(unit.baseDeck);
        unit.hand = new int[0];
        unit.discard = new int[0];
    }

    void handleUnitTurn(Unit self, Unit foe, int inputIndex, double multiplier, Game game){
        int spellIndex = self.hand[inputIndex];
        Spell spellToCast = game.theBook.allSpells[spellIndex];
        castSpell(spellToCast,self,foe,multiplier);
        //discardACard(self,inputIndex);
        self.discard = append(self.discard,spellIndex);
        self.hand = remove(self.hand,inputIndex);
        // self.hand = rebuild(self.hand,length(self.hand)-1);
        drawCard(self,1);
        delay(2500);
    }

    void remakeDeck(Unit unit){
        int len = length(unit.deck)+length(unit.discard);
        int[] res = new int[len];
        for(int index = 0; index<len; index++){
            if(index<length(unit.deck)) res[index] = unit.deck[index];
            else res[index] = unit.discard[index-length(unit.deck)];
        }
        unit.deck = shuffle(res);
        unit.discard = new int[0];
    }

    void applyBuffs(Unit unit){
        for(int buffIndex = 0; buffIndex<length(unit.buffList); buffIndex++){
            if(unit.buffList[buffIndex]!=null){

                final Buff buff = unit.buffList[buffIndex];
                final int buffPower = unit.buffList[buffIndex].power;

                buff.duration--;

                switch(buff.buffType){
                    case BLEED:
                        if(unit.buffList[buffIndex].duration == 0){
                            unit.health-=buffPower;
                            println("Ses plaies explosent, "+unit.name+"subit "+buff.power+" dégats de saignement!");
                        }
                        else{
                            println(unit.name+" continue de saigner!");
                        }
                        break;

                    case POISON:
                        int poisonDamage = (int)(unit.health*(0.1*buffPower));
                        unit.health-=poisonDamage;
                        println(unit.name+" subit "+poisonDamage+" dégats de poison!");
                        break;
                    
                    case SHOCK:
                        if(unit.buffList[buffIndex].duration == 0){
                            int shockDamage = buffPower;
                            unit.health-=shockDamage;
                            println(unit.name+" subit "+shockDamage+" dégats de foudroiement!");
                        }
                        break;

                    case REGEN:
                        int healAmount = buffPower;
                        healDamage(unit,healAmount);
                        println(unit.name+" régénère "+healAmount+" PV!");
                        break;

                    case SHIELD:
                        if(buffPower == 0) unit.buffList[buffIndex] = null;
                        break;

                    default: //CONCUSS IGNITE
                        break;
                }
            }
            if(unit.buffList[buffIndex] != null) if(unit.buffList[buffIndex].duration == 0) unit.buffList[buffIndex] = null;
        }
    }

    void healDamage(Unit unit, int amount){
        unit.health = clamp(unit.health+amount,0,unit.maxHealth);
    }

    void importStats(Unit unit, String fileName){
        CSVFile statFile = loadCSV(fileName,',');

        String deckID = unit.name;
        int index = 0;
        String rowID;
        do{
            rowID = getCell(statFile,index,0);
            index++;
        }
        while(index<rowCount(statFile) && !rowID.equals(deckID));

        if(index>=rowCount(statFile)) throw new RuntimeException("DeckID \""+deckID+"\" was not found");

        unit.maxHealth = Integer.parseInt(getCell(statFile,index-1,1));
        unit.health = unit.maxHealth;
        unit.strength = Double.parseDouble(getCell(statFile,index-1,2));
    }

    boolean isAlive(Unit unit){
        return unit.health>0;
    }

    void handleEnemyDeath(Game game){
            game.level++;
            game.playerUnit.maxHealth += level*2;
            println(game.enemyUnit.name+" disparait!");
            println("Vous gagnez "+(level*2)+" PV max!");
            delay(2000);
            String nextOpponentName = MONSTER_NAMES_LIST[game.level];
            game.enemyUnit = newUnit(nextOpponentName);
            game.enemyUnit.gameLink = game;
            importDeck(game.enemyUnit,DECK_FILE,game.theBook);
            importStats(game.enemyUnit,MONTERS_STATS);
            switchGameState(GameState.SHOP,game);
            game.shopActions = 2;
    }

    void handlePlayerDeath(Game game){
        game.run = false;
    }

    //EFFECT METHODS--------------------------------------------------------------------------------------------
    String toString(Effect type){
        String res = "";
        switch(type){
            case DAMAGE:
                res="dégats";
                break;
            case HEAL:
                res="soins";
                break;
            case SHIELD:
                res="boucliers";
                break;
            case BLEED:
                res="saignement";
                break;
            case POISON:
                res="empoisonnement";
                break;
            case SHOCK:
                res="dégats de choc";
                break;
            case CONCUSS:
                res="tours d'armure réduite";
                break;
            case IGNITE:
                res="embrasement";
                break;
            case REGEN:
                res="régénération";
                break;
            case RIGHTNESS:
                res="exactitude";
                break;

        }
        return res;
    }

    //TARGET METHODS--------------------------------------------------------------------------------------------
    String toString(Target type){
        String res = "";
        switch(type){
            case SELF:
                res="soi même";
                break;
            case FOE:
                res="l'adversaire";
                break;
        }
        return res;
    }

    Unit targetSwitch(Target type, Unit self, Unit foe){
        Unit res = null;
        switch(type){
            case SELF:
                res = self;
                break;
            case FOE:
                res = foe;
                break;
        }
        return res;
    }

    //BUFF METHODS--------------------------------------------------------------------------------------------
    Buff newBuff(int duration, int power, Effect buffType){
        Buff res = new Buff();
        res.duration = duration;
        res.power = power;
        res.buffType = buffType;
        return res;
    }

    String toString(Buff buff){
        String res = "";
        res+="("+buff.duration+" tours) ";
        switch(buff.buffType){
            case SHIELD:
                res+="Bouclier de résistance "+buff.power;
                break;

            case BLEED:
                res+="Saignement de "+buff.power+" dégats";
                break;

            case POISON:
                res+="Empoisonnement de "+(buff.power*10)+"%";
                break;
            
            case SHOCK:
                res+="Foudroiement de "+buff.power+" dégats";
                break;
            
            case CONCUSS:
                res+="Armure réduite";
                break;
            
            case IGNITE:
                res+="Embrasement";
                break;
            case REGEN:
                res+="Régénération de "+buff.power+" PV";
                break;
        }
        return res;
    }

    //SPELL METHODS--------------------------------------------------------------------------------------------
    Spell newSpell(String name, Ability[] abilities){
        Spell res = new Spell();
        res.name = name;
        res.spellAbilities = abilities;
        return res;
    }

    String toString(Spell spell){
        String res = "";
        res +=spell.name+"\n";
        for(int i = 0; i<length(spell.spellAbilities); i++){
            res+="  "+toString(spell.spellAbilities[i])+"\n";
        }
        return res;
    }

    void castSpell(Spell spell,Unit self, Unit foe, double multiplicator){
        println(self.name+" a lancé "+spell.name+" !");
        final int abilityCount = length(spell.spellAbilities);
        for(int i = 0; i<abilityCount; i++){
            Ability ability = spell.spellAbilities[i];
            Unit targetUnit = targetSwitch(ability.targetType,self,foe);
            executeAbility(ability,targetUnit,multiplicator);
        }
    }

    int getSpellLevel(Spell spell){
        int res = 1;
        final int spellNameLength = length(spell.name);
        final char identifier = spell.name.charAt(spellNameLength-1);
        if(identifier>='1' && identifier<='9'){
            res = Character.getNumericValue(identifier);
        }
        return res;

    }

    //SPRITE METHODS--------------------------------------------------------------------------------------------
    Sprite newEmptySprite(int width, int height){
        Sprite res = new Sprite();
        res.width = width;
        res.height = height;
        res.image = new String[height];
        return res;
    }

    Sprite importSprite(String fileName){
        File importedFile = newFile(fileName);
        int lineNumber = lineCount(fileName);

        Sprite res = new Sprite();
        res.image = new String[lineNumber];
        res.height = lineNumber;
        int maxWidth = 0;


        String line = readLine(importedFile);

        int index = 0;
        while(line!=null){
            res.image[index] = line;
            maxWidth = max(maxWidth,length(line));
            line = readLine(importedFile);

            index++;
        }
        res.width = maxWidth;
        return res;
    }

    String toString(Sprite sprite){
        String res = "";
        for(int index = 0; index<sprite.height; index++){
            res += sprite.image[index]+"\n";
        }
        return res;
    }

    Sprite castSprite(Sprite targetSprite, Sprite sourceSprite, int x, int y){
        //casts a sourceSprite into a targetSprite
        Sprite res = newEmptySprite(targetSprite.width,targetSprite.height);
        for(int idy = 0; idy<targetSprite.height; idy++){
            res.image[idy]="";
            for(int idx = 0; idx<targetSprite.width; idx++){
                if((idx>=x && idx<x+sourceSprite.width)&&(idy>=y && idy<y+sourceSprite.height)){
                    res.image[idy]+= sourceSprite.image[idy-y].charAt(idx-x);
                }
                else res.image[idy] += targetSprite.image[idy].charAt(idx);
            }
        }
        return res;
    }

    Sprite castSpriteExp(Sprite targetSprite, Sprite sourceSprite, int x, int y){
        //casts a sourceSprite into a targetSprite, targetSprite is enlarged if it is too small for the casted sprite(sprite casted at negative coordinates will not show up)
        int widthOverflow = max((x + sourceSprite.width), targetSprite.width);
        int heightOverflow = max((y + sourceSprite.height), targetSprite.height);

        Sprite res = newEmptySprite(widthOverflow,heightOverflow);
        for(int idy = 0; idy<heightOverflow; idy++){
            res.image[idy]="";
            for(int idx = 0; idx<widthOverflow; idx++){
                if((idx>=x && idx<x+sourceSprite.width)&&(idy>=y && idy<y+sourceSprite.height)){
                    res.image[idy]+= sourceSprite.image[idy-y].charAt(idx-x);
                }
                else{
                    char appenedChar = ' ';
                    if ((idx>=0 && idx<targetSprite.width)&&(idy>=0 && idy<targetSprite.height)){
                        appenedChar = targetSprite.image[idy].charAt(idx);
                    }
                    res.image[idy] += appenedChar;
                }
            }
        }
        return res;
    }
    
    ///QUESTION METHODS--------------------------------------------------------------------------------------------
    Question newQuestion(String[] importedData){
        Question res = new Question();
        res.askLine = importedData[0];
        res.answer = importedData[1];
        res.answerList = new String[4];
        for(int index = 0; index<4; index++){
            res.answerList[index] = importedData[index+1];
        }
        return res;
        
    }

    Question[] importQuestionList(String fileName){
        CSVFile importedFile = loadCSV(fileName);
        int questionCount = rowCount(importedFile);
        Question[] res = new Question[questionCount];
        for(int lineIndex = 0; lineIndex<questionCount; lineIndex++){
            String[] line = new String[5];
            for(int colIndex = 0; colIndex<5; colIndex++){
                line[colIndex] = getCell(importedFile, lineIndex, colIndex);
            }
            res[lineIndex] = newQuestion(line);
            shuffle(res[lineIndex].answerList);
        }
        return res;
    }

    boolean answerIsValid(Question question, int input){
        return question.answerList[input].equals(question.answer);
    }

    String toString(Question question){
        String res = "";
        res+=ASCIILINE+"\n";
        res+=question.askLine+"\n";
        for(int index = 0; index<4; index++){
            res+=(index+1)+": "+question.answerList[index]+"\n";
        }
        res+=ASCIILINE+"\n";
        return res;
    }

    //SPELLBOOK METHODS--------------------------------------------------------------------------------------------
    SpellBook initialiseSpellBook(){
        SpellBook res = new SpellBook();
        CSVFile loadedSpells = loadCSV("ressources/spellList.csv",',');
        res.allSpells = new Spell[rowCount(loadedSpells)-1];
        for(int i = 0; i<rowCount(loadedSpells)-1; i++){
            String name = getCell(loadedSpells,i+1,0);
            Ability[] effects = importAbilities(getCell(loadedSpells,i+1,1)) ;
            res.allSpells[i] = newSpell(name,effects);
        }
        return res;
    }

    String toString(SpellBook book){
        String res = ASCIILINE;
        res+="Liste de tous les sorts éxistants:\n";
        for(int i = 0; i<length(book.allSpells); i++){
            res+=i+": "+toString(book.allSpells[i])+"\n";
        }
        res+= ASCIILINE;
        return res;
    }

    Ability[] importAbilities(String data){
        //parses a stringed list of abilities dumped from spellList.csv, and put them into a list 
        int abilityCount = 0;
        for(int i = 0; i<length(data);i++){
            if(data.charAt(i)==';') abilityCount++;
        }

        Ability[] res = new Ability[abilityCount];
        
        for(int i = 0;i<abilityCount;i++){
            int strOffset = i*12;
            String effect = data.substring(strOffset,strOffset+3);
            int power = Integer.parseInt(data.substring(strOffset+4,strOffset+7));
            String target = data.substring(strOffset+8,strOffset+11);
            res[i] = newAbility(effect,power,target);
        }

        return res;
    }

    int getSpellIndex(SpellBook book, String spellName){
        int index = 0;
        while(index<length(book.allSpells) && !book.allSpells[index].name.equals(spellName)){
            index++;
        }
        if(index==length(book.allSpells)) throw new RuntimeException("Spell \""+spellName+"\" was not found");
        return index;
    }

    int getDeckSpellCount(String fileName, String deckID){
        File importedFile = newFile(fileName);
        int res = -1;

        String line;
        do{
            line = readLine(importedFile);
        }while(line != null && !line.equals(deckID));

        do{
            line = readLine(importedFile);
            res++;
        }
        while(line != null && !line.equals(""));
        
        return res;
    }
    //GAMELOGIC--------------------------------------------------------------------------------------------
    void initTITLE(Game game){
        println(toString(game.titleScreen));
        println("Choissez une option");
        println("1.Lancer le jeu");
        println("2.Afficher la liste des sorts");
        println("3.Afficher la liste des questions");
        println("4.Quitter le jeu");
        game.initGameState = false;
    }

    void logicTITLE(Game game){
        int userInput = getPlayerInput(4);
        switch(userInput){
            case 0:
                switchGameState(GameState.COMBAT,game);
                break;
            case 1:
                switchGameState(GameState.SPELLIST,game);
                break;
            case 2:
                switchGameState(GameState.QUESLIST,game);
                break;
            case 3:
                game.run = false;
                break;
        }
    }
    
    void initCOMBAT(Game game){
        resetDeck(game.playerUnit);
        shuffle(game.playerUnit.deck);
        drawCard(game.playerUnit,4);


        resetDeck(game.enemyUnit);
        shuffle(game.enemyUnit.deck);
        drawCard(game.enemyUnit,4);

        game.initGameState = false;
    }

    void logicCOMBAT(Game game){
        //turn start
        clearScreen();

        applyBuffs(game.playerUnit);
        if(!isAlive(game.playerUnit)) handlePlayerDeath(game);

        println(toString(game.playerUnit));
        println(toString(game.enemyUnit));

        if(game.debug){
            print("Player hand: ");
            println(game.playerUnit.hand);
            print("Player deck: ");
            println(game.playerUnit.deck);
            print("Player discard: ");
            println(game.playerUnit.discard);
        }
        //enemy action selection
        game.enemyNextAttack = (int)(random()*4);

        String enemySpellName = game.theBook.allSpells[game.enemyUnit.hand[game.enemyNextAttack]].name;

        println("L'adversaire s'apprête à lancer "+ enemySpellName+"\n");

        //playerturn
        
        println("Choisissez le sort que vous aller lancer");
        for(int index = 0; index<length(game.playerUnit.hand); index++){
            int elemIdx = game.playerUnit.hand[index];
            Spell elem = game.theBook.allSpells[elemIdx];
            println((index+1)+": "+toString(elem));
        }
        int handLen = length(game.playerUnit.hand);
        int userInput = getPlayerInput(handLen);

        clearScreen();
        handleUnitTurn(game.playerUnit,game.enemyUnit,userInput,game.playerUnit.strength,game);
        if(!isAlive(game.playerUnit)) handlePlayerDeath(game);
        if(!isAlive(game.enemyUnit)) handleEnemyDeath(game);

        if(game.gameState != GameState.SHOP) switchGameState(GameState.QUESTION,game);
    }

    void initSHOP(Game game){
        if(game.debug) println(game.playerUnit.baseDeck);
        println(toString(game.playerUnit));
        println("Vous arrivez à un camp! Que souhaitez vous faire? (vous pouvez encore réaliser "+game.shopActions+" actions):");
        println("-------Apprendre de nouveaux sorts (1 parmis ceux là):-------");
        int spellCount = length(game.theBook.allSpells);
        final int shopSize = 3;
        int[] selectedSpells = new int[shopSize];
        for(int i = 0; i<shopSize; i++){
            int randomSpell = (int)(random()*(spellCount/2))*2;
            selectedSpells[i] = randomSpell;
            println((i+1)+": "+toString(game.theBook.allSpells[randomSpell]));
        }
        println("-------Panser vos plaies:-------");
        println("4: Vous soigner de 25% de vos PV max");
        println();
        println("-------Vous préparer:-------");
        println("5: Améliorer un sort de votre deck");
        println();
        println("-------Méditer:-------");
        println("6: Oublier 1 sort de votre deck");
        println();
        game.shopList = selectedSpells;
        game.initGameState = false;
    }

    void logicSHOP(Game game){
        int userInput = getPlayerInput(6);

        if(userInput<3){
            int selectedSpellIndex = game.shopList[userInput];
            Spell selectedSpell = game.theBook.allSpells[selectedSpellIndex];
            println("Vous ajoutez "+ selectedSpell.name+" à votre deck!");
            game.playerUnit.baseDeck = append(game.playerUnit.baseDeck,selectedSpellIndex);
            delay(2500);
            switchGameState(GameState.SHOP,game);
            game.shopActions -=1;
        }
        else{
            switch(userInput){
                case 3:
                    int healAmount = game.playerUnit.maxHealth / 4;
                    healDamage(game.playerUnit,healAmount);
                    println("Vous vous soignez de "+healAmount+" PV");
                    delay(2500);
                    switchGameState(GameState.SHOP,game);
                    game.shopActions -=1;
                    break;
                case 4:
                    switchGameState(GameState.UPGRADE,game);
                    break;
                case 5:
                    switchGameState(GameState.OBLIVION,game);
                    break;
            }
        }
    }

    void initSPELLIST(Game game){
        println(toString(game.theBook));
        println("1.Retour");
        game.initGameState = false;
    }

    void logicSPELLIST(Game game){
        int userInput = getPlayerInput(1);

        switch(userInput){
            case 0:
                switchGameState(GameState.TITLE,game);
                break;
        }
    }

    void initQUESLIST(Game game){
        for(int index = 0; index<length(game.questionList); index++){
            println(toString(game.questionList[index]));
        }
        println("1.Retour");
        game.initGameState = false;
    }

    void logicQUESLIST(Game game){
        int userInput = getPlayerInput(1);

        switch(userInput){
            case 0:
                switchGameState(GameState.TITLE,game);
                break;
        }
    }

    void initQUESTION(Game game){
        int randomIndex = (int)(random()*length(game.questionList));
        game.currentQuestion = game.questionList[randomIndex];
        println(toString(game.currentQuestion));
        game.initGameState = false;
    }

    void logicQUESTION(Game game){
        int userInput = getPlayerInput(4);

        clearScreen();
        boolean rightAnswer = answerIsValid(game.currentQuestion,userInput);

        double answerMultiplier = game.enemyUnit.strength;
        if(rightAnswer){
            println("Bonne réponse! Vous subissez moins de dégats");
            game.playerUnit.buffList[BUFFID_RIGHTNESS] = newBuff(1,0,Effect.RIGHTNESS);
        } 
        else println("Mauvaise réponse! Vous subissez plus de dégats");

        applyBuffs(game.enemyUnit);
        if(isAlive(game.enemyUnit)){
            handleUnitTurn(game.enemyUnit, game.playerUnit,game.enemyNextAttack, answerMultiplier,game);

            switchGameState(GameState.COMBAT,game);
            game.initGameState = false;
        }
        else{
            handleEnemyDeath(game);
        }                
    }

    void initUPGRADE(Game game){
        println("Choisissez le sort que vous allez améliorer:");
        for(int index = 0; index<length(game.playerUnit.baseDeck); index++){
            int spellIndex = game.playerUnit.baseDeck[index];
            Spell element = game.theBook.allSpells[spellIndex];
            println((index+1)+": "+toString(element));
        }
        game.initGameState = false;
    }

    void logicUPGRADE(Game game){
        int userInput = getPlayerInput(length(game.playerUnit.baseDeck));

        int spellIndex = game.playerUnit.baseDeck[userInput];
        Spell choosenSpell = game.theBook.allSpells[spellIndex];
        int spellLevel = getSpellLevel(choosenSpell);
        if (spellLevel == 1){
            game.playerUnit.baseDeck = remove(game.playerUnit.baseDeck,spellIndex);
            String upgradedSpellName = choosenSpell.name + " 2";
            int upgradedSpellIndex = getSpellIndex(game.theBook,upgradedSpellName);
            game.playerUnit.baseDeck = append(game.playerUnit.baseDeck,upgradedSpellIndex);
            println("Le sort "+choosenSpell.name+" a été amélioré en "+ upgradedSpellName +" !");
            delay(2500);
            switchGameState(GameState.SHOP,game);
            game.shopActions -=1;
        }
        else{
            println("Le sort a déjà été amélioré!");
            delay(2500);
            switchGameState(GameState.SHOP,game);
        }
    }

    void initOBLIVION(Game game){
        println("Choisissez un sort à oublier:");
        for(int index = 0; index<length(game.playerUnit.baseDeck); index++){
            int spellIndex = game.playerUnit.baseDeck[index];
            Spell element = game.theBook.allSpells[spellIndex];
            println((index+1)+": "+toString(element));
        }
        game.initGameState = false;
    }

    void logicOBLIVION(Game game){
        int userInput = getPlayerInput(length(game.playerUnit.baseDeck));

        int spellIndex = game.playerUnit.baseDeck[userInput];
        Spell choosenSpell = game.theBook.allSpells[spellIndex];
        game.playerUnit.baseDeck = remove(game.playerUnit.baseDeck,spellIndex);
        println("Le sort "+choosenSpell.name+" a été retiré du deck!");
        delay(2500);
        switchGameState(GameState.SHOP,game);
        game.shopActions -=1;
    }



    //MAINLOOP--------------------------------------------------------------------------------------------
    void algorithm(){
        clearScreen();
        Game game = new Game();
        game.theBook = initialiseSpellBook();
        
        game.level=0;

        game.playerUnit = newUnit("PLAYER");
        game.enemyUnit = newUnit(MONSTER_NAMES_LIST[game.level]);
        game.playerUnit.gameLink = game;
        game.enemyUnit.gameLink = game;

        game.questionList = importQuestionList("ressources/questions.csv");
        game.currentQuestion = null;


        game.titleScreen = importSprite("ressources/spellAskerTitle.txt");
        // final Sprite blankSquare = importSprite("ressources/blankSquare.txt");

        importDeck(game.playerUnit,DECK_FILE,game.theBook);
        importDeck(game.enemyUnit,DECK_FILE,game.theBook);
        importStats(game.playerUnit,MONTERS_STATS);
        importStats(game.enemyUnit,MONTERS_STATS);

        show();
        while(game.run){
            switch(game.gameState){
                case TITLE:
                    if(game.initGameState){
                        initTITLE(game);
                    }
                    logicTITLE(game);
                    break;

                case COMBAT:
                    //combat initialisation
                    if(game.initGameState){                        
                        initCOMBAT(game);
                    }
                    logicCOMBAT(game);
                    
                    break;

                case SHOP:
                    if(game.shopActions>0){
                        if(game.initGameState){
                            initSHOP(game);
                        }
                        logicSHOP(game);
                    }
                    else{
                        switchGameState(GameState.COMBAT,game);
                    }
                    break;

                case SPELLIST:
                    if(game.initGameState){
                        initSPELLIST(game);
                    }
                    logicSPELLIST(game);
                    break;

                case QUESLIST:
                    if(game.initGameState){
                        initQUESLIST(game);
                    }
                    logicQUESLIST(game);
                    break;

                case QUESTION:
                    if(game.initGameState){
                        initQUESTION(game);
                    }
                    logicQUESTION(game);
                    break;

                case UPGRADE:
                    if(game.initGameState){
                        initUPGRADE(game);
                    }
                    logicUPGRADE(game);
                    break;

                case OBLIVION:
                    if(length(game.playerUnit.baseDeck)>4){
                        if(game.initGameState){
                            initOBLIVION(game);
                        }
                        logicOBLIVION(game);
                        
                    }
                    else{
                        println("Vous ne pouvez pas oublier plus de sorts de votre deck (votre deck est déjà trop petit)!");
                        delay(2500);
                        switchGameState(GameState.SHOP,game);
                    }
                    
                    break;
            }
        }
        clearScreen();
        println("GameOver! vous avez perdu.");
    }
}