--%  ft| TRAJECTORY_T0_V0_A1_O1 sn| Closed~curve~orbit n| #0 d| 2 n| #1 d| 3 n| #2 d| 1 n| #3 d| 2 n| #4 d| 4 n| #5 d| 3 n| #6 d| 0.01 n| #7 d| 0 n| #8 d| 1000 n| #9 d| 1000 n| #10 d| x n| #11 d| y 
--@@
name = "Lotka - Volterra"
description = "See Model refs in user's guide1"
parameters = {"alpha", "beta", "gamma", "delta"}
variables = {"x", "y"}
type = "C"

function f(alpha, beta, gamma, delta, x, y)

    x1 = alpha * x - beta * x * y
    y2 = - gamma * y + delta * x * y

    return x1, y2

end

