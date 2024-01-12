class Game{
    GameState gameState = GameState.TITLE;
    boolean initGameState = true;
    boolean run = true;
    boolean error = false;
    
    boolean debug = true;

    String[] ennemyList = new String[] {"WOLF","WOLF","WOLF"};
    Question[] questionList;
    Question currentQuestion;

    int[] shopList;
/*
    int[] cardChoiceList;
    int cardChoiceResult;
    */
    SpellBook theBook;
    Unit playerUnit;
    Unit enemyUnit;
    int enemyNextAttack;
}