import extensions.CSVFile;
import ijava.Curses;
class Main extends Program{
    final String ASCIILINE = "-----------------------------------------------\n";
    
    Ability newAbility(String effect, int power, String target){
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
            default:
                println("Error in spellList: malformed effect");
                type = null;
                break;
        }
        res.effectType = type;
        res.power = (int) power;
        
        Target targ;
        switch(target){
            case "PLR":
                targ = Target.PLAYER;
                break;
            case "FOE":
                targ = Target.ENNEMY;
                break; 
            default:
                println("Error in spellList: malformed target");
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
        res+=" to ";
        res+= toString(ability.targetType);

        return res;
    }
    String toString(Effect type){
        String res = "";
        switch(type){
            case DAMAGE:
                res="damage";
                break;
            case HEAL:
                res="heal";
                break;
            case SHIELD:
                res="shield";
                break;
        }
        return res;
    }
    String toString(Target type){
        String res = "";
        switch(type){
            case PLAYER:
                res="player";
                break;
            case ENNEMY:
                res="ennemy";
                break;
        }
        return res;
    }

    Ability[] importAbilities(String data){

        int abilityCount = 1;
        for(int i = 0; i<length(data);i++){
            if(data.charAt(i)==',') abilityCount++;
        }

        Ability[] res = new Ability[abilityCount];
        
        for(int i = 0;i<abilityCount;i++){
            String effect = data.substring(0,3);
            int power = Character.getNumericValue(data.charAt(4));
            String target = data.substring(6,9);
            res[i] = newAbility(effect,power,target);
        }

        return res;
    }

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

    SpellBook initialiseSpellBook(){
        SpellBook res = new SpellBook();
        CSVFile loadedSpells = loadCSV("src/spellList.csv",',');
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
        res+="List of all available spells:\n";
        for(int i = 0; i<length(book.allSpells); i++){
            res+=toString(book.allSpells[i])+"\n";
        }
        res+= ASCIILINE;
        return res;
    }

    void algorithm(){
        SpellBook theBook = initialiseSpellBook();
        println(toString(theBook));
    }
}