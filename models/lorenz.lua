--%  ft| TRAJECTORY_T0_V0_A1_O1 sn| Lorenz~"butterfly"~attractor~x~VS~z~axis n| #0 d| 1 n| #1 d| 2 n| #2 d| 1 n| #3 d| 10 n| #4 d| 28 n| #5 d| 2.667 n| #6 d| 0.005 n| #7 d| 1000 n| #8 d| 20000 n| #9 d| 20000 n| #10 d| x n| #11 d| z 
--%  ft| TRAJECTORY_T0_V0_A1_O1 sn| Convergence~to~a~fixed~point n| #0 d| 15 n| #1 d| 15 n| #2 d| 10 n| #3 d| 10 n| #4 d| 28 n| #5 d| 4 n| #6 d| 0.005 n| #7 d| 0 n| #8 d| 8000 n| #9 d| 8000 n| #10 d| x n| #11 d| y 
--%  ft| BIFURCATION_1 sn| Chaotic~and~periodic~dynamics~ n| #0 d| 15 n| #1 d| 15 n| #2 d| 10 n| #3 d| 10 n| #4 d| 28 n| #5 d| b n| #6 d| 2.5 n| #7 d| 5 n| #8 d| -30 n| #9 d| 30 n| #10 d| 0 0 1 27 n| #11 d| 150 n| #12 d| 100 n| #13 d| 0.005 n| #14 d| x 
--@@
name = "Lorenz"
description = "See Model refs in user's guide"
type = "C"
parameters = {"sigma", "r", "b"}
variables = {"x", "y", "z"}

function f(sigma, r, b, x, y, z)

    x1 = - sigma * (x - y)
    y1 = x * (r - z) - y
    z1 = x * y - b * z

    return x1, y1, z1

end

function Jf(sigma, r, b, x, y, z)

	return   

	-sigma,	sigma,	0,
	r - z,	-1,		-x,
	y,		x,		-b

end

