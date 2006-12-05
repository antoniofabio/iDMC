--%  ft| BIFURCATION_1 sn| Period-doubling~route~to~chaos n| #0 d| 1 n| #1 d| 1 n| #2 d| 1 n| #3 d| 0.4 n| #4 d| 6 n| #5 d| c n| #6 d| 0.35 n| #7 d| 0.88 n| #8 d| -0.25 n| #9 d| 1.6 n| #10 d| 500 n| #11 d| 200 n| #12 d| x 
--%  ft| TRAJECTORY_T0_V0_A1_O0 sn| Chaotic~attractor n| #0 d| 0 n| #1 d| 1 n| #2 d| 1 n| #3 d| 0.4 n| #4 d| 0.9 n| #5 d| 6 n| #6 d| 100 n| #7 d| 20000 n| #8 d| 20000 n| #9 d| x n| #10 d| y 
--@@

name = "Ikeda"
description = "See Model refs in user's guide"
type = "D"

parameters = {"a", "b", "c", "d"}

variables = {"x", "y"}


function f(a, b, c, d, x, y)

    r = b - d / (1.0 + x^2 + y^2)

    x1 = a + c * (x * math.cos(r) - y * math.sin(r))
    y1 = c * (x * math.sin(r) + y * math.cos(r))

    return x1, y1

end
