--%  ft| BIFURCATION_1 sn| Neimark-Sacker~bifurcation n| #0 d| 0.1 n| #1 d| 0.2 n| #2 d| 1.2 n| #3 d| mu n| #4 d| 4.5 n| #5 d| 8.75 n| #6 d| -0.05 n| #7 d| 0.75 n| #8 d| 200 n| #9 d| 100 n| #10 d| c 
--%  ft| TRAJECTORY_T0_V0_A1_O0 sn| Quasiperiodic~orbit n| #0 d| 0.1 n| #1 d| 0.7 n| #2 d| 5.25 n| #3 d| 1.2 n| #4 d| 100 n| #5 d| 5000 n| #6 d| 5000 n| #7 d| c n| #8 d| l 
--@@
name = "Olgns"
description = "See Model refs in user's guide"
type = "D"
parameters = {"mu", "b"}
variables = {"c", "l"}

function f (mu, b, c, l)

	c1 = l^mu
	l1 = b * ( l - c )
 
	return c1, l1

end

function Jf (mu, b, c, l)

	return

	0,	mu * l^(mu - 1),
	-b,	b

end



