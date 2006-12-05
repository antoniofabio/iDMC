--%  ft| TRAJECTORY_T0_V0_A1_O1 sn| Convergence~to~a~fixed~point n| #0 d| 1 n| #1 d| .5 n| #2 d| -0.2 n| #3 d| -1 n| #4 d| 0.05 n| #5 d| 0 n| #6 d| 5000 n| #7 d| 5000 n| #8 d| x n| #9 d| y 
--%  ft| TRAJECTORY_T0_V0_A1_O1 sn| Convergence~to~a~limit~cycle n| #0 d| 1 n| #1 d| 1 n| #2 d| .5 n| #3 d| -1.2 n| #4 d| 0.01 n| #5 d| 0 n| #6 d| 5000 n| #7 d| 5000 n| #8 d| x n| #9 d| y 
--@@
name = "Hopf"
description = "See Model refs in user's guide"
type = "C"
parameters = {"mu", "k"}
variables = {"x", "y"}

function f(mu, k, x, y)

    x1 = y + k*x * (x^2 + y^2)
    x2 = -x + mu*y

    return x1, x2

end

