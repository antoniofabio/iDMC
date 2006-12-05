--%  ft| TRAJECTORY_T1_V0_A1_O0 sn| Chaotic~trajectory n| #0 d| 0.3 n| #1 d| 0.2 n| #2 d| 1 n| #3 d| 0 n| #4 d| 100 n| #5 d| 100 n| #6 d| x 
--%  ft| BIFURCATION_1 sn| Chaotic~window n| #0 d| .1 n| #1 d| 1 n| #2 d| c1 n| #3 d| -0.1 n| #4 d| 1.1 n| #5 d| -0.1 n| #6 d| 1.1 n| #7 d| 500 n| #8 d| 200 n| #9 d| x 
--@@
name = "Tent"
description = "See Model refs in user's guide"
type = "D"
parameters = {"c1", "c2"}
variables = {"x"}

function f(c1, c2, x)

	if (x <= c1) then 
		y = (c2 / c1) * x
	end

	if (x > c1) then
		y = (c2 / (1 - c1)) * (1 - x)
	end

	return y

end
