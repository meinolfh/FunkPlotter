package de.mherbst.funkplotter.math;

public final class DualNumber {

    // Forward Mode Automatic Differentiation mit dual numbers
    //
    // Allgemein soll sein: x = a + εb
    //      Wir wählen im Code immer b=1, d.h. es handelt sich immer um die erste normale Ableitung nach x
    //      Für b=2 erhalten wir das zweifache der 1. Ableitung, d.h. die Ableitung skaliert proportional
    //       mit dem Dual-Eingang
    //      Für b=-1 dreht sich das Vorzeichen der Ableitung um
    //      Für b=0.5 erhält man 50% der normalen Ableitung
    // Ferner soll sein: y = c + εd (analog zu oben d=1)
    // Für die Dualzahl ε gilt:
    //  ε <> 0                              (1)
    //  ε^2 = 0                             (2)
    //
    // Taylorreihe einer stetigen und differenzierbaren Funktion:
    //
    //          infinity
    //         _________     (n)
    //         \            f   (a)
    //  f(x) =  |           ------- * (x-a)^n
    //         /               n!
    //         ---------
    //          n = 0
    //
    // Voraussetzungen für die Anwendung von Taylor:
    // 1. Stetigkeit und Differenzierbarkeit (unendlich oft) im Punkt a und in einer Umgebung von a, d.h. keine Pole, Sprungstellen
    // 2. Die Taylorreihe muss für das betrachtete x konvergieren, d.h. |x-a| < R (Konvergenzradius)
    //
    // Voraussetzungen bei automatischer Differenzierung mit Dualzahlen
    // 1. f(x) muss an der Stelle a differenzierbar sein
    // 2. Die erste Ableitung muss existieren
    // 3. Es darf keine Singularität bei x=a geben
    //
    // Dann ist:
    // T{f(x)} = f(a) + f'(a) * (x-a)^1 + f''(a) * (x-a)^2 + ...
    // mit x = a + εb gilt:
    // T{f(a+ε)} = f(a) + f'(a) * (a+εb-a) + f''(a) * (a+εb-a)^2 + ... = f(a) + f'(a) * (εb) + f''(a) * (εb)^2 + ...
    // Wegen (2) sind alle Summanden ab f''(a) = 0, also gilt insgesamt:
    //
    //                                      f(a+ε) = f(a) + εb * f'(a)            (3)

    private double real;
    private double dual;

    // Konstruktoren
    public DualNumber () {
        this.real = 0.0;
        this.dual = 0.0;
    }

    public DualNumber ( double real, double dual ) {
        this.real = real;   // Reeller Teil f(x)
        this.dual = dual;   // Dualer Teil f'(x)
    }

    // Getter und Setter
    public double getReal () {
        return real;
    }

    public double getDual () {
        return dual;
    }

    public void setReal ( double real ) {
        this.real = real;
    }

    public void setDual ( double dual ) {
        this.dual = dual;
    }

    // Addition: f(x) + f(y) = (a+εb) + (c+εd) = (a+c) + ε(b+d)
    public DualNumber add ( DualNumber dn ) {
        final double real = getReal ( ) + dn.getReal ( );
        final double dual = getDual ( ) + dn.getDual ( );
        return new DualNumber ( real, dual );
    }

    // Multiplikation: f(x) * f(y) = (a+εb) * (c+εd) = a*c + a*εd + εb*c + ε^2bd = a*c + ε(a*d + b*c)
    public DualNumber multiply ( DualNumber dn ) {
        final double real = getReal ( ) * dn.getReal ( );
        final double dual = getReal ( ) * dn.getDual ( ) + getDual ( ) * dn.getReal ( );
        return new DualNumber ( real, dual );
    }

    // Exponential: f(x) = e^(a+εb) = (1+εb)*e^a
    public DualNumber exp () {
        final double real = Math.exp ( getReal ( ) );
        final double dual = real * getDual ( );
        return new DualNumber ( real, dual );
    }

    // Wurzel: f(x) = exp ( 1 / (a+εb) )
    public DualNumber exp_1_over_x () {
        if ( getReal ( ) == 0.0 ) {
            return new DualNumber ( Double.POSITIVE_INFINITY, Double.NaN );
        }
        final double real = Math.exp ( 1.0 / getReal ( ) );
        final double dual = -real * getDual ( ) / ( getReal ( ) * getReal ( ) );
        return new DualNumber ( real, dual );
    }

    // Sinus: f(x) = sin(a+εb) = sin(a) + εb*cos(a)
    public DualNumber sin () {
        final double real = Math.sin ( getReal ( ) );
        final double dual = getDual ( ) * Math.cos ( getReal ( ) );
        return new DualNumber ( real, dual );
    }

    // Cosinus: f(x) = cos(a+εb) = cos(a) - εb*sin(a)
    public DualNumber cos () {
        final double real = Math.cos ( getReal ( ) );
        final double dual = -getDual ( ) * Math.sin ( getReal ( ) );
        return new DualNumber ( real, dual );
    }

    // Quadrat: f(x) = (a+εb)^2 = a^2 + 2*a*b*ε + (εb)^2
    public DualNumber square () {
        final double real = getReal ( ) * getReal ( );
        final double dual = 2.0 * getReal ( ) * getDual ( );
        return new DualNumber ( real, dual );
    }

    // Potenz: f(x) = (a+εb)^n = a^n + n*a^(n-1)*b*ε
    public DualNumber pow ( int n ) {
        final double real = Math.pow ( getReal ( ), n );
        final double dual = n * Math.pow ( getReal ( ), n - 1 ) * getDual ( );
        return new DualNumber ( real, dual );
    }

    @Override
    public String toString () {
        return String.format ( "{ real=%f, dual=ε * %f* }", real, dual );
    }
}