--%  ft| TRAJECTORY_T1_V0_A1_O1 sn| Convergence~to~a~fixed~point n| #0 d| 0.4 n| #1 d| 1 n| #2 d| 2 n| #3 d| 4 n| #4 d| 3 n| #5 d| 0.01 n| #6 d| 0 n| #7 d| 150 n| #8 d| 150 n| #9 d| p 
--@@
name = "Conpar"
description = "See Model refs in user's guide"
type = "C"
parameters = {"a", "m", "b", "s"}
variables = {"p"}

function f(a, m, b, s, p)

	p1 = a + m - (b + s) * p
	
	return p1
end

function Jf(a, m, b, s, p)

return - b - s

end
