--%  ft| BIFURCATION_1 sn| Route~to~chaos~by~a~Neimark-Sacker~bifurcation n| #0 d| 0.2 n| #1 d| 0.1 n| #2 d| 0.1 n| #3 d| epsilon n| #4 d| 1.27 n| #5 d| 1.48 n| #6 d| -0.2 n| #7 d| 1.05 n| #8 d| 500 n| #9 d| 100 n| #10 d| x 
--%  ft| TRAJECTORY_T0_V0_A1_O0 sn| Chaotic~attractor n| #0 d| 0.2 n| #1 d| 0.1 n| #2 d| -1.375 n| #3 d| 1.8 n| #4 d| 100 n| #5 d| 10000 n| #6 d| 10000 n| #7 d| x n| #8 d| y 
--@@
name = "MS"
description = "See Model refs in user's guide"
type = "D"
parameters = {"epsilon", "mu"}
variables = {"x", "y"}

function f (epsilon, mu, x, y)

	x1 = epsilon * x + mu - y^2
	y1 = x

return x1, y1

end

function Jf (epsilon, mu, x, y)

	return  

	epsilon,	-2 * y,
 	1,		 0

end



