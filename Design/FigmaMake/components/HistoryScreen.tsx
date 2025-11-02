import { Calendar } from 'lucide-react';
import { Card } from './ui/card';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

interface HistoryScreenProps {
  onNavigate: (screen: string) => void;
}

interface HistoryItem {
  id: string;
  date: string;
  time: string;
  calories: number;
  items: string[];
}

export function HistoryScreen({ onNavigate }: HistoryScreenProps) {
  const weeklyData = [
    { day: 'Mon', calories: 1850 },
    { day: 'Tue', calories: 2100 },
    { day: 'Wed', calories: 1950 },
    { day: 'Thu', calories: 2200 },
    { day: 'Fri', calories: 1900 },
    { day: 'Sat', calories: 2400 },
    { day: 'Sun', calories: 2050 },
  ];

  const history: HistoryItem[] = [
    {
      id: '1',
      date: 'Nov 2, 2025',
      time: '12:30 PM',
      calories: 650,
      items: ['Grilled Chicken', 'Brown Rice', 'Broccoli', 'Olive Oil'],
    },
    {
      id: '2',
      date: 'Nov 2, 2025',
      time: '8:15 AM',
      calories: 420,
      items: ['Oatmeal', 'Banana', 'Almonds'],
    },
    {
      id: '3',
      date: 'Nov 1, 2025',
      time: '7:45 PM',
      calories: 780,
      items: ['Salmon', 'Quinoa', 'Asparagus', 'Avocado', 'Lemon'],
    },
    {
      id: '4',
      date: 'Nov 1, 2025',
      time: '1:00 PM',
      calories: 550,
      items: ['Turkey Sandwich', 'Apple', 'Carrots'],
    },
    {
      id: '5',
      date: 'Oct 31, 2025',
      time: '6:30 PM',
      calories: 720,
      items: ['Pasta', 'Tomato Sauce', 'Meatballs', 'Parmesan'],
    },
  ];

  return (
    <div className="flex flex-col h-full bg-gray-50">
      {/* Header */}
      <div className="bg-white border-b px-6 py-4">
        <h1 className="text-gray-900">History & Insights</h1>
        <p className="text-gray-500 text-sm mt-1">Track your progress</p>
      </div>

      {/* Content */}
      <div className="flex-1 overflow-auto px-6 py-6 space-y-6">
        {/* Weekly Chart */}
        <div>
          <h2 className="text-gray-900 mb-4">Calories This Week</h2>
          <div className="bg-white rounded-lg border border-gray-200 p-4">
            <ResponsiveContainer width="100%" height={200}>
              <LineChart data={weeklyData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
                <XAxis
                  dataKey="day"
                  tick={{ fontSize: 12, fill: '#6b7280' }}
                  stroke="#d1d5db"
                />
                <YAxis
                  tick={{ fontSize: 12, fill: '#6b7280' }}
                  stroke="#d1d5db"
                />
                <Tooltip
                  contentStyle={{
                    backgroundColor: 'white',
                    border: '1px solid #e5e7eb',
                    borderRadius: '0.5rem',
                  }}
                />
                <Line
                  type="monotone"
                  dataKey="calories"
                  stroke="#2563eb"
                  strokeWidth={2}
                  dot={{ fill: '#2563eb', r: 4 }}
                />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Past Analyses */}
        <div>
          <h2 className="text-gray-900 mb-4">Past Analyses</h2>
          <div className="space-y-3">
            {history.map((item) => (
              <Card
                key={item.id}
                className="p-4 bg-white border border-gray-200 cursor-pointer hover:shadow-md transition-shadow"
                onClick={() => onNavigate('result')}
              >
                <div className="flex items-start gap-3">
                  <div className="w-10 h-10 bg-blue-50 rounded-lg flex items-center justify-center flex-shrink-0">
                    <Calendar className="h-5 w-5 text-blue-600" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex justify-between items-start mb-2">
                      <div>
                        <p className="text-gray-900">{item.date}</p>
                        <p className="text-sm text-gray-500">{item.time}</p>
                      </div>
                      <p className="text-gray-900">{item.calories} kcal</p>
                    </div>
                    <p className="text-sm text-gray-600 truncate">
                      {item.items.join(', ')}
                    </p>
                  </div>
                </div>
              </Card>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
