--%  ft| TRAJECTORY_T0_V0_A1_O0 sn| Chaotic~attractor n| #0 d| 5 n| #1 d| 2 n| #2 d| 72 n| #3 d| 0.5 n| #4 d| 2 n| #5 d| 100 n| #6 d| 10000 n| #7 d| 10000 n| #8 d| l n| #9 d| c 
--%  ft| BIFURCATION_1 sn| Period-doubling~route~to~chaos n| #0 d| 1 n| #1 d| 2 n| #2 d| 0.5 n| #3 d| 2 n| #4 d| r n| #5 d| 30 n| #6 d| 74 n| #7 d| 2.5 n| #8 d| 5.5 n| #9 d| 500 n| #10 d| 200 n| #11 d| l 
--@@
name = "Olg1"
description = "See Model refs in user's guide"
type = "D"
parameters = {"r","gamma", "b"}
variables = {"l", "c"}

function f (r, gamma, b, l, c)

	l1 = (r*c*math.exp(-c))^gamma
	c1 = (r*c*math.exp(-c))^gamma-(1/b)*l
 
	return l1, c1

end

function Jf (r, gamma, b, l, c)

	return

	0,		(gamma*(r*c*math.exp(-c))^(gamma-1))*r*math.exp(-c)*(1-c),
	-(1/b),	(gamma*(r*c*math.exp(-c))^(gamma-1))*r*math.exp(-c)*(1-c) 

end

