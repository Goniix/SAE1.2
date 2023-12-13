import extensions.CSVFile;
class Main extends Program{
    
    Ability newAbility(String effect, char power, String target){
        Ability res = new Ability();
        Effects type;
        switch(effect){
            case "DMG":
                type = Effects.DAMAGE;
                break;
            case "HEL":
                type = Effects.HEAL;
                break; 
            case "SHD":
                type = Effects.SHIELD;
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

    Ability[] importAbilities(String data){

        int abilityCount = 1;
        for(int i = 0; i<length(data);i++){
            if(data.charAt(i)==',') abilityCount++;
        }

        Ability[] res = new Ability[abilityCount];
        
        for(int i = 0;i<abilityCount;i++){
            String effect = data.substring(0,3);
            char power = data.charAt(4);
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

    SpellBook initialiseSpellBook(){
        SpellBook res = new SpellBook();
        CSVFile loadedSpells = loadCSV("src/spellList.csv",',');
        res.allSpells = new Spell[rowCount(loadedSpells)];
        for(int i = 0; i<rowCount(loadedSpells); i++){
            String name = getCell(loadedSpells,0,i);
            Ability[] effects = importAbilities(getCell(loadedSpells,1,i)) ;
            res.allSpells[i].name = name;
            res.allSpells[i].spellAbilities = effects;
        }
        return res;
    }

    void algorithm(){
        SpellBook theBook = initialiseSpellBook();
    }
}
