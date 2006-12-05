--%  ft| TRAJECTORY_T0_V0_A1_O1 sn| Chaotic~Rossler~attractor~x~VS~y~axis n| #0 d| 0 n| #1 d| 0 n| #2 d| 0 n| #3 d| .25 n| #4 d| .2 n| #5 d| 5.7 n| #6 d| 0.02 n| #7 d| 0 n| #8 d| 20000 n| #9 d| 20000 n| #10 d| x n| #11 d| y 
--%  ft| TRAJECTORY_T0_V0_A1_O1 sn| Chaotic~Rossler~attractor~y~VS~z~axis n| #0 d| 0 n| #1 d| 0 n| #2 d| 0 n| #3 d| .2 n| #4 d| .2 n| #5 d| 5.8 n| #6 d| 0.02 n| #7 d| 0 n| #8 d| 20000 n| #9 d| 20000 n| #10 d| y n| #11 d| z 
--%  ft| TRAJECTORY_T0_V0_A1_O1 sn| Periodic~orbit n| #0 d| 1 n| #1 d| 1 n| #2 d| 5 n| #3 d| 0.2 n| #4 d| 0.2 n| #5 d| 4.1 n| #6 d| 0.02 n| #7 d| 8000 n| #8 d| 10000 n| #9 d| 10000 n| #10 d| x n| #11 d| y 
--%  ft| BIFURCATION_1 sn| Route~to~chaos n| #0 d| 0 n| #1 d| 0 n| #2 d| 0 n| #3 d| 0.2 n| #4 d| 0.2 n| #5 d| c n| #6 d| 2.5 n| #7 d| 5 n| #8 d| -3 n| #9 d| 10 n| #10 d| 0 0 1 1 n| #11 d| 50 n| #12 d| 300 n| #13 d| 0.01 n| #14 d| x 
--@@
name = "Rossler"
description = "See Model refs in user's guide"
type = "C"
parameters = {"a", "b", "c"}
variables = {"x", "y", "z"}

function f(a, b, c, x, y, z)

	x1 = - (y + z)
	y1 = x + (a * y)
	z1 = b + z * (x - c)

	return x1, y1, z1

end

function Jf(a, b, c, x, y, z)

	return
   
	0,	-1,	1,
	1,	a,	0,
	z,	0,	x - c

end
