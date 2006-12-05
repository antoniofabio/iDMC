--%  ft| BASIN sn| a:~1~~infi:~111~~tran:~30~~iter:~11111~~tria:~100~~min:~-1~~max:~1~~min:~-1~~ n| #0 d| 1 n| #1 d| 111 n| #2 d| 30 n| #3 d| 11111 n| #4 d| 100 n| #5 d| -1 n| #6 d| 1 n| #7 d| -1 n| #8 d| 1 
--%  ft| TRAJECTORY_T0_V0_A1_O0 sn| Invariant~curves n| #0 d| -0.45 n| #1 d| 0.3 n| #2 d| 1.33 n| #3 d| 1000 n| #4 d| 30000 n| #5 d| 30000 n| #6 d| x n| #7 d| y 
--%  ft| BIFURCATION_1 sn| Closed-curve~asimpthtotic~dynamics n| #0 d| -0.45 n| #1 d| 0.3 n| #2 d| a n| #3 d| 0.8 n| #4 d| 5.75 n| #5 d| -1 n| #6 d| 1 n| #7 d| 500 n| #8 d| 200 n| #9 d| x 
--@@
name = "Cremona"
description = "See Model refs in user's guide"
type = "D"
parameters = {"a"}
variables = {"x", "y"}

function f(a, x, y)

	x1 = x * math.cos(a) - (y - x^2) * math.sin (a)
	y1 = x * math.sin(a) + (y - x^2) * math.cos(a)

	return x1, y1

end

