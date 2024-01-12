class Game{
    GameState gameState = GameState.TITLE;
    boolean initGameState = true;
    boolean run = true;
    boolean error = false;

    String gameoverMessage;
    
    boolean debug = false;

    Question[] questionList;
    Question currentQuestion;

    int[] shopList;
    int shopActions;

    int level;
/*
    int[] cardChoiceList;
    int cardChoiceResult;
    */
    SpellBook theBook;
    Unit playerUnit;
    Unit enemyUnit;
    int enemyNextAttack;

    //SPRITES
    Sprite titleScreen;
}