import random;

length = 30
vehicles = 100
steps = 10000
print("%d %d %d" % (length, vehicles, steps))

for i in range(vehicles):
    ts = random.randint(1, 50)
    direction_mapper = {
            0: 'N',
            1: 'E',
            2: 'S',
            3: 'W'
            }
    fr = direction_mapper[random.randint(0,3)]
    T1 = random.randint(1,50)
    to = direction_mapper[random.randint(0,3)]
    Ts = random.randint(1,50)
    haste = '?'
    type_mapper = {
            0: 'R',
            1: 'H',
            2: 'N',
            3: 'F',
            4: 'A'
            }
    typ = type_mapper[random.randint(0,4)]
    max_speed = 3
    points = random.randint(10, 50)
    print("%d %s %d %s %d %s %s %d %d" % (ts, fr, T1, to, Ts, haste, typ, max_speed, points))


