import extensions.CSVFile;
import extensions.File;
import ijava.Curses;
class Splask extends Program{
    final String ASCIILINE = "-----------------------------------------------";
    final String DECK_FILE = "ressources/deckList.txt";
    final int BUFFID_SHIELD = 0;
    final int BUFFID_BLEED = 1;
    final int BUFFID_POISON = 2;
    final int BUFFID_SHOCK = 3;
    final int BUFFID_CONCUSS = 4;
    final int BUFFID_IGNITE = 5;
    
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

    char getPlayerInput(char max){
        char res;
        do{
            res = readChar();
        }while(res>max || res<'1');
        return res;
    }

    int[] shuffle(int[] list){
        int len = length(list);
        for(int index = 0; index<len; index++){
            int randomIndex = (int) (random()*len);
            int temp = list[index];
            list[index] = list[randomIndex];
            list[randomIndex] = temp;
        }
        return list;
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

    void println(int[] list){
        String res = "[";
        for(int index = 0; index<length(list); index++){
            res+=list[index]+", ";
        }
        println(res+"]");
    }

    int[] copy(int[] list){
        int[] res = new int[length(list)];
        for(int index = 0; index<length(list); index++){
            res[index] = list[index];
        }
        return res;
    }

    //GAMESTATE INPUTS--------------------------------------------------------------------------------------------
    void input(char key, Game game){
        switch(game.gameState){
            case TITLE:
                switch(key){
                    case '1':
                        switchGameState(GameState.COMBAT,game);
                        break;
                    case '2':
                        switchGameState(GameState.SPELLIST,game);
                        break;
                    case '3':
                        switchGameState(GameState.QUESLIST,game);
                        break;
                    case '4':
                        game.run = false;
                        break;
                }
                break;
            case SPELLIST:
                switch(key){
                    case '1':
                        switchGameState(GameState.TITLE,game);
                        break;
                }
                break;
            case QUESLIST:
                switch(key){
                    case '1':
                        switchGameState(GameState.TITLE,game);
                        break;
                }
                break;
            case COMBAT:
                if(key>='1' && key<='4'){
                    clearScreen();
                    int inputIndex = Character.getNumericValue(key)-1;
                    handleUnitTurn(game.playerUnit,game.enemyUnit,inputIndex,game.playerUnit.strength,game);

                }
                break;
            case QUESTION:
                if(key>='1' && key<='4'){
                    int inputIndex = Character.getNumericValue(key)-1;
                    boolean rightAnswer = answerIsValid(game.currentQuestion,inputIndex);
                    double answerMultiplier = (rightAnswer) ? game.enemyUnit.strength- 0.4: game.enemyUnit.strength;
                    
                    handleUnitTurn(game.enemyUnit, game.playerUnit,game.enemyNextAttack,answerMultiplier,game);
                    /*
                    int spellIndex = game.enemyUnit.hand[game.enemyNextAttack];
                    Spell spellToCast = game.theBook.allSpells[spellIndex];
                    castSpell(spellToCast, game.enemyUnit, game.playerUnit, answerMultiplier);
                    discardACard(game.enemyUnit,game.enemyNextAttack);
                    game.enemyUnit.hand = rebuildPile(game.enemyUnit.hand,length(game.enemyUnit.hand)-1);
                    drawCard(game.enemyUnit,1);
                    delay(2500);*/

                    //ok c'est immonde faut nest ça dans une fonction ou reuse handleUnitTurn
                    switchGameState(GameState.COMBAT,game);
                    game.initGameState = false;
                }
                break;
        }
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
            default:
                println("Error malformed effect: "+effect);
                type = null;
                break;
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
                println("Error malformed target: "+target);
                targ = null;
                break;
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
                    power *= 1.3;
                    basePower *= 1.3;
                }

                if(targetUnit.buffList[BUFFID_SHIELD] != null){
                    power = max(0, power-targetUnit.buffList[BUFFID_SHIELD].power);
                    targetUnit.buffList[BUFFID_SHIELD].power -= basePower-power;
                }
                targetUnit.health -= power;

                

                println(targetUnit.name+" subit "+basePower+" dégats");

                if(targetUnit.buffList[BUFFID_BLEED] != null){
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
                    targetUnit.health = clamp(targetUnit.health+power,0,targetUnit.maxHealth);
                    println(targetUnit.name+" se soigne de "+power+" HP");

                }
                else{
                    //is burning
                    println(targetUnit.name+" brule! Il ne parvient pas à refermer ses plaies!");
                }

                break;

            case SHIELD:
                //targetUnit.shield += power;
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
                    targetUnit.buffList[BUFFID_BLEED] = newBuff(3,0,type);
                    println(targetUnit.name+" se met à saigner!");
                }
                else{
                    targetUnit.buffList[BUFFID_BLEED].duration += 1;
                    println(targetUnit.name+" continue de saigner!");
                }
                break;

            case POISON:
                targetUnit.buffList[BUFFID_POISON] = newBuff(3,power,type);
                println(targetUnit.name+" est empoisonné! Il perd "+(power*10)+"% de vie par tour!");
                break;
            
            case SHOCK:
                if(targetUnit.buffList[BUFFID_SHOCK] == null){
                    targetUnit.buffList[BUFFID_SHOCK] = newBuff(2,power,type);
                }
                else{
                    targetUnit.buffList[BUFFID_SHOCK].power += power;
                }
                println(targetUnit+" subira "+targetUnit.buffList[BUFFID_SHOCK].power+" dégats au début de son prochain tour!");
                break;
            
            case CONCUSS:
                targetUnit.buffList[BUFFID_CONCUSS] = newBuff(power,3,type);
                println(targetUnit.name+" est étourdit! Il subit 30% de dégats en plus!");
                break;
            
            case IGNITE:
                targetUnit.buffList[BUFFID_IGNITE] = newBuff(2,0,type);
                println(targetUnit.name + " brûle! Il ne peut plus se soigner!");
                break;

                
        }
    }

    //UNIT METHODS--------------------------------------------------------------------------------------------
    Unit newUnit(String name){
        Unit res = new Unit();
        res.name = name;
        res.maxHealth = 100;
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

        int deckSize = (line!=null) ? Integer.parseInt(readLine(importedFile)) : 0;
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
            if(unit.name.equals("PLAYER")){
                int drawedSpellIndex = unit.hand[length(unit.hand)-1];
                String drawedSpellName = unit.gameLink.theBook.allSpells[drawedSpellIndex].name;
                println("Vous piochez "+drawedSpellName);
            }
            unit.deck = rebuildPile(unit.deck, length(unit.deck)-1);
        }
    }

    void resetDeck(Unit unit){
        unit.deck = copy(unit.baseDeck);
        unit.hand = new int[0];
    }

    void discardACard(Unit unit, int index){
        //unit.discard = rebuildPile(unit.discard, length(unit.discard)+1);
        unit.discard = append(unit.discard,unit.hand[index]);
        unit.hand[index]=-1;
    }

    void handleUnitTurn(Unit self, Unit foe, int inputIndex, double multiplier, Game game){
        int spellIndex = self.hand[inputIndex];
        Spell spellToCast = game.theBook.allSpells[spellIndex];
        castSpell(spellToCast,self,foe,multiplier);
        discardACard(self,inputIndex);
        self.hand = rebuildPile(self.hand,length(self.hand)-1);
        drawCard(self,1);
        delay(2500);
    }
    
    int[] rebuildPile(int[] pile, int cardCount){
        int[] newPile = new int[cardCount];
        int refIndex = 0;
        int oldIndex = 0;
        while(refIndex<cardCount && oldIndex<length(pile)){
            if(pile[oldIndex] != -1){
                newPile[refIndex] = pile[oldIndex];
                refIndex++;
            }
            oldIndex++;
        }
        return newPile;
    }

    int[] append(int[] pile, int element){
        int[] res = rebuildPile(pile,length(pile)+1);
        res[length(pile)] = element;
        return res;
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

                unit.buffList[buffIndex].duration--;

                Buff buff = unit.buffList[buffIndex];
                switch(buff.buffType){
                    case BLEED:
                        if(unit.buffList[buffIndex].duration == 0){
                            unit.health-=unit.buffList[buffIndex].power;
                        }
                        println("Ses plaies explosent, "+unit.name+"subit "+buff.power+" dégats de saignement!");
                        break;

                    case POISON:
                        int poisonDamage = (int)(unit.health*(0.1*unit.buffList[buffIndex].power));
                        unit.health-=poisonDamage;
                        println(unit.name+" subit "+poisonDamage+" dégats de poison!");
                        break;
                    
                    case SHOCK:
                        if(unit.buffList[buffIndex].duration == 0){
                            int shockDamage = unit.buffList[buffIndex].power;
                            unit.health-=shockDamage;
                            println(unit.name+" subit "+shockDamage+" dégats de foudroiement!");
                        }
                        break;

                    default: //SHIELD CONCUSS IGNITE
                        break;
                }
                if(unit.buffList[buffIndex].duration == 0) unit.buffList[buffIndex] = null;
            }
            
        }
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
                res="tours d'étourdissement";
                break;
            case IGNITE:
                res="embrasement";
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
                res+="Etourdissement";
                break;
            
            case IGNITE:
                res+="Embrasement de "+buff.power+" dégats";
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
        res +=spell.name+" : ";
        for(int i = 0; i<length(spell.spellAbilities); i++){
            res+=toString(spell.spellAbilities[i])+", ";
        }
        return res;
    }

    void castSpell(Spell spell,Unit self, Unit foe, double multiplicator){
        println(self.name+" a lancé "+spell.name+" !");
        int abilityCount = length(spell.spellAbilities);
        for(int i = 0; i<abilityCount; i++){
            Ability ability = spell.spellAbilities[i];
            Unit targetUnit = targetSwitch(ability.targetType,self,foe);
            executeAbility(ability,targetUnit,multiplicator);
        }
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
        return (index==length(book.allSpells)) ? -1 : index;
    }


    //MAINLOOP--------------------------------------------------------------------------------------------
    void algorithm(){
        clearScreen();
        Game game = new Game();
        game.theBook = initialiseSpellBook();

        game.playerUnit = newUnit("PLAYER");
        game.enemyUnit = newUnit("WOLF");
        game.playerUnit.gameLink = game;
        game.enemyUnit.gameLink = game;

        game.questionList = importQuestionList("ressources/questions.csv");
        game.currentQuestion = null;


        Sprite titleScreen = importSprite("ressources/spellAskerTitle.txt");
        Sprite blankSquare = importSprite("ressources/blankSquare.txt");

        importDeck(game.playerUnit,DECK_FILE,game.theBook);
        importDeck(game.enemyUnit,DECK_FILE,game.theBook);

        //int testIndex = 0;
        show();
        while(game.run){
            
            /*
            try{
                clearScreen();
                print(toString(castSprite(titleScreen, blankSquare,testIndex%titleScreen.width,0)));
                Thread.sleep(16);
            }
            catch(InterruptedException e){
                println(e);
            }
            testIndex++;
            */
            
            
            switch(game.gameState){
                case TITLE:
                    if(game.initGameState){
                        println(toString(titleScreen));
                        println("Choissez une option");
                        println("1.Lancer le jeu");
                        println("2.Afficher la liste des sorts");
                        println("3.Afficher la liste des questions");
                        println("4.Quitter le jeu");
                        game.initGameState = false;
                    }
                    input(getPlayerInput('4'),game);
                    break;
                case MAP:
                    if(game.initGameState){

                        game.initGameState = false;
                    }
                    break;
                case COMBAT:
                    //combat initialisation
                    if(game.initGameState){
                        resetDeck(game.playerUnit);
                        shuffle(game.playerUnit.deck);
                        drawCard(game.playerUnit,4);


                        resetDeck(game.enemyUnit);
                        shuffle(game.enemyUnit.deck);
                        drawCard(game.enemyUnit,4);

                        game.initGameState = false;
                    }


                    //turn start
                    clearScreen();
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
                    char handLen = (char)(length(game.playerUnit.hand)+'0');
                    input(getPlayerInput(handLen),game);

                    switchGameState(GameState.QUESTION,game);
                    
                    break;
                case SHOP:
                    if(game.initGameState){
                        
                        game.initGameState = false;
                    }
                    break;
                case SPELLIST:
                    if(game.initGameState){
                        println(toString(game.theBook));
                        println("1.Retour");
                        game.initGameState = false;
                    }
                    input(getPlayerInput('1'),game);
                    break;
                case QUESLIST:
                    if(game.initGameState){
                        for(int index = 0; index<length(game.questionList); index++){
                            println(toString(game.questionList[index]));
                        }
                        println("1.Retour");
                        game.initGameState = false;
                    }
                    input(getPlayerInput('1'),game);
                    break;
                case QUESTION:
                    if(game.initGameState){
                        int randomIndex = (int)(random()*length(game.questionList));
                        game.currentQuestion = game.questionList[randomIndex];
                        println(toString(game.currentQuestion));
                    }
                    input(getPlayerInput('4'),game);
                    break;
            }
        }
    }
}