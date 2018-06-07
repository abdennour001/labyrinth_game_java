import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import java.util.Scanner;
import java.util.Random;
import java.util.ArrayList;

public class LabyGraphics extends JFrame implements ActionListener, KeyListener{


    panel p=new panel();
    static int iii=1;
    static int jjj=0;
    final int N = 35;
    final int M = 70;

    Case[][] labyrinthe = new Case[N][M];
    boolean first = true;
    public Timer t=new Timer(50, this);
    static final Scanner input = new Scanner(System.in);
    static final Random random = new Random();

    /**
     * Les différents états possibles d'une case :
     *   MUR      il y a un mur
     *   VIDE     il n'y a rien
     *   CONSTR   case constructible
     *
     * On utilise ici une énumeration pour définir un nouveau type ayant pour
     * valeurs ces constantes.  Cf. tout bon manuel de Java pour les détails.
     */
    enum Case { MUR, VIDE, CONSTR,};

    /**
     * Retourne `true' si la case (i, j) est constructible
     */
    static boolean estConstructible(Case[][] laby, int i, int j) {
        // dimensions du labyrinthe
        final int N = laby.length;
        final int M = laby[0].length;

        return
                // la case n'est pas déjà construite
                (laby[i][j] != Case.MUR) &&
                        // la case n'est pas au bord
                        (i > 0 && i < N - 1 && j > 0 && j < M - 1) &&
                        ( // la case à droite est un mur ?
                                ( laby[i][j+1] == Case.MUR   && laby[i-1][j] != Case.MUR &&
                                        laby[i-1][j-1] != Case.MUR && laby[i][j-1] != Case.MUR &&
                                        laby[i+1][j-1] != Case.MUR && laby[i+1][j] != Case.MUR ) ||
                                        // la case au dessus est un mur ?
                                        ( laby[i-1][j] == Case.MUR   && laby[i][j-1] != Case.MUR &&
                                                laby[i+1][j-1] != Case.MUR && laby[i+1][j] != Case.MUR &&
                                                laby[i+1][j+1] != Case.MUR && laby[i][j+1] != Case.MUR ) ||
                                        // la case à gauche est un mur ?
                                        ( laby[i][j-1] == Case.MUR   && laby[i+1][j] != Case.MUR &&
                                                laby[i+1][j+1] != Case.MUR && laby[i][j+1] != Case.MUR &&
                                                laby[i-1][j+1] != Case.MUR && laby[i-1][j] != Case.MUR ) ||
                                        // la case au dessous est un mur ?
                                        ( laby[i+1][j] == Case.MUR && laby[i][j+1] != Case.MUR &&
                                                laby[i-1][j+1] != Case.MUR && laby[i-1][j] != Case.MUR &&
                                                laby[i-1][j-1] != Case.MUR && laby[i][j-1] != Case.MUR ) );
    }

    /**
     * Recherche la `k'-ième case (k >= 1) dans `laby' ayant la valeur
     * `val'.  Ne considère pas les cases du bord.  Les coordonnées de la
     * case trouvée sont retournées par un tableau de deux entiers.
     * Retourne { -1, -1 } si la case en question n'existe pas.
     */
    static int[] trouveCase(Case[][] laby, int k, Case val) {
        // dimensions du labyrinthe
        final int N = laby.length;
        final int M = laby[0].length;

        int[] res = { -1, -1 };
        recherche:
        for (int i = 1 ; i < N - 1 ; i++) {
            for (int j = 1 ; j < M - 1 ; j++) {
                if (laby[i][j] == val)
                    k--;
                if (k == 0) {
                    res[0] = i;
                    res[1] = j;
                    break recherche;
                }
            }
        }
        return res;
    }

    /**
     * Initialise le labyrinthe : place un mur tout autour, sauf à
     * l'entrée et à la sortie.
     */
    static void initLaby(Case[][] laby) {
        // dimensions du labyrinthe
        final int N = laby.length;
        final int M = laby[0].length;

        // coordonnées de l'entrée du labyrinthe
        final int Ei = 1;
        final int Ej = 0;

        // coordonnées de la sortie du labyrinthe
        final int Si = (N - 2);
        final int Sj = (M - 1);

        for (int i = 0 ; i < N ; i++) {
            for (int j = 0 ; j < M ; j++) {
                if ( (i == 0 || i == N - 1 || j == 0 || j == M - 1) &&
                        (i != Ei || j != Ej) && (i != Si || j != Sj) )
                    laby[i][j] = Case.MUR;
                else
                    laby[i][j] = Case.VIDE;
            }
        }
    }

    /**
     * Place `ngraines' graines de manière aléatoire.
     */
    static void placeGraines(Case[][] laby, int ngraines) {
        // dimensions du labyrinthe
        final int N = laby.length;
        final int M = laby[0].length;

        int nvides = (N - 2) * (M - 2); // le nombre de cases vides
        if (ngraines > nvides)
            ngraines = nvides;
        while (ngraines-- > 0) {
            // choisit une case de manière aléatoire parmi les cases vides
            int[] c = trouveCase(laby, 1 + random.nextInt(nvides), Case.VIDE);
            int i = c[0];
            int j = c[1];
            // construit un mur sur cette case
            laby[i][j] = Case.MUR;
            nvides--;
        }
    }

    /**
     * Marque et dénombre les cases constructibles.  Retourne leur nombre.
     */
    static int marqueCons(Case[][] laby) {
        // dimensions du labyrinthe
        final int N = laby.length;
        final int M = laby[0].length;

        int ncons = 0;              // le nombre de cases constructibles
        // NB: les cases du bord ne sont pas constructibles, il est
        // inutile de les tester
        for (int i = 1 ; i < N - 1; i++) {
            for (int j = 1 ; j < M - 1; j++) {
                if (estConstructible(laby, i, j)) {
                    laby[i][j] = Case.CONSTR;
                    ncons++;
                }
            }
        }
        return ncons;
    }

    /**
     * Construit le labyrinthe.
     */
    static void genereLaby(Case[][] laby) {
        // dimensions du labyrinthe
        final int N = laby.length;
        final int M = laby[0].length;

        int ncons = marqueCons(laby); // le nombre de cases constructibles
        while (ncons > 0) {
            // choisit une case de manière aléatoire parmi les cases
            // constructibles
            int[] c = trouveCase(laby, 1 + random.nextInt(ncons), Case.CONSTR);
            int i = c[0];
            int j = c[1];
            // construit un mur sur cette case
            laby[i][j] = Case.MUR;
            ncons--;
            // met à jour les huit cases autour
            for (int ii = i - 1; ii <= i + 1; ii++) {
                for (int jj = j - 1; jj <= j + 1; jj++) {
                    if (laby[ii][jj] == Case.CONSTR &&
                            !estConstructible(laby, ii, jj)) {
                        laby[ii][jj] = Case.VIDE;
                        ncons--;
                    } else if (laby[ii][jj] == Case.VIDE &&
                            estConstructible(laby, ii, jj) ) {
                        laby[ii][jj] = Case.CONSTR;
                        ncons++;
                    }
                }
            }
        }
    }

    class panel extends JPanel {
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            // taille du labyrinthe : N lignes, M colonnes

      /*System.out.print("Nombre de graines ? ");
      ngraines = input.nextInt();*/


            if(first) {
                initLaby(labyrinthe);
                placeGraines(labyrinthe, 12);
                genereLaby(labyrinthe);
            }
            //---------------
            afficheLaby(labyrinthe, g);
        }

        /**
         * Affiche le labyrinthe sur la sortie standard
         */
        void afficheLaby(Case[][] laby, Graphics g) {
            // dimensions du labyrinthe
            final int N = laby.length;
            final int M = laby[0].length;
            final int widthCase = 15;
            final int heightCase = 15;

            int i, j;                   // coordonnées d'une case
            laby[iii][jjj] = Case.MUR;
            for (i = 0 ; i < N ; i++) {
                for (j = 0 ; j < M ; j++) {
                    if (laby[i][j] == Case.MUR) {
                        //System.out.print('#');
                        if((i == iii )&&(j == jjj)) {
                            g.setColor(Color.RED);
                        }else{
                            g.setColor(Color.BLACK);
                        }
                        g.fillRect(j*widthCase, i*heightCase, widthCase, heightCase);
                    }
                }
            }
        }
    }

    public LabyGraphics () {
        super("Labyrient mode graphic");
        setSize(1055, 556);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        add(p);
        setVisible(true);
        addKeyListener(this);
        t.start();
    }

    public static void main(String[] args) {
        new LabyGraphics() ;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        p.repaint();
        first = false;
    }
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        if((code == KeyEvent.VK_RIGHT)&&(labyrinthe[iii][jjj + 1] != Case.MUR)) {
            labyrinthe[iii][jjj] = Case.VIDE;
            jjj++;
        }
        if((code == KeyEvent.VK_LEFT)&&(labyrinthe[iii][jjj - 1] != Case.MUR)) {
            labyrinthe[iii][jjj] = Case.VIDE;
            jjj--;
        };
        if((code == KeyEvent.VK_DOWN)&&(labyrinthe[iii + 1][jjj] != Case.MUR)) {
            labyrinthe[iii][jjj] = Case.VIDE;
            iii++;
        };
        if((code == KeyEvent.VK_UP)&&(labyrinthe[iii - 1][jjj] != Case.MUR)) {
            labyrinthe[iii][jjj] = Case.VIDE;
            iii--;
        };
    }
    @Override
    public void keyReleased(KeyEvent e) {

    }
    @Override
    public void keyTyped(KeyEvent e) {

    }
}
