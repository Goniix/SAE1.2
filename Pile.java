class Pile{
    int[] c = new int[0];

    public void rebuild(int size){
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
        this.c = newPile;
    }

    public void append(int elem){
        this.rebuild(this.length()+1);
        this.c[this.length()-1] = elem;
    }

    public int length(){
        return length(this.c);
    }

    public int pop(){
        int res = this.c[this.length()-1];
        this.rebuild(this.length()-1);
        return res;
    }

    public void shuffle(){
        for(int index = 0; index<len; index++){
            int randomIndex = (int) (random()*this.length());
            int temp = this.c[index];
            this.c[index] = this.c[randomIndex];
            this.c[randomIndex] = temp;
        }
    }
}