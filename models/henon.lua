--%  ft| TRAJECTORY_T0_V0_A1_O0 sn| Chaotic~Hénon~attractor n| #0 d| 0.5 n| #1 d| 1 n| #2 d| 1.42 n| #3 d| 0.3 n| #4 d| 1000 n| #5 d| 10000 n| #6 d| 1000 n| #7 d| x n| #8 d| y 
--%  ft| BIFURCATION_1 sn| Period-doubling~route~to~chaos n| #0 d| 0.5 n| #1 d| 1 n| #2 d| 0.3 n| #3 d| a n| #4 d| 0.3 n| #5 d| 1.45 n| #6 d| -2 n| #7 d| 2 n| #8 d| 500 n| #9 d| 200 n| #10 d| x 
--@@

name = "Hénon"
description = "See Model refs in user's guide"
type = "D"
parameters = {"a", "b"}
variables = {"x", "y"}


function f(a, b, x, y)

    x1 = a - x^2 + b * y
    y1 = x

    return x1, y1

end


function Jf(a, b, x, y)

	return 

	-2 * x,	b,
	1,		0

end

-- inverse 
function g(a, b, x, y)

    x1 = y
    y1 = (x - a + y^2) / b

    return x1, y1

end




