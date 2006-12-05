--%  ft| TRAJECTORY_T1_V0_A1_O0 sn| Convergence~to~a~fixed~point n| #0 d| 8 n| #1 d| 10 n| #2 d| 0.5 n| #3 d| 2 n| #4 d| 0.99 n| #5 d| 0 n| #6 d| 15 n| #7 d| 15 n| #8 d| p 
--@@
name = "Dispar"
description = "See Model refs in user's guide"
type = "D"
parameters = {"a", "b", "m", "s"}
variables = {"p"}

function f(a, b, m, s, p)

	y = a + m + (1 - b - s) * p

	return y

end

function Jf(a, b, m, s, p)

	return 1 - b - s
end


