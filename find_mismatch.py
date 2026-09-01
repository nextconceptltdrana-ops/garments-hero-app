import sys
c = 0
for i, line in enumerate(open(sys.argv[1]).readlines(), 1):
    c += line.count('{') - line.count('}')
    if i == 601: print(f"Count at 601: {c}")
