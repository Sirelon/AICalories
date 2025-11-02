import { Camera, Image, Clock } from 'lucide-react';
import { Button } from './ui/button';
import { Card } from './ui/card';

interface RecentAnalysis {
  id: string;
  date: string;
  time: string;
  totalCalories: number;
  itemsCount: number;
}

interface HomeScreenProps {
  onNavigate: (screen: string) => void;
}

export function HomeScreen({ onNavigate }: HomeScreenProps) {
  const recentAnalyses: RecentAnalysis[] = [
    {
      id: '1',
      date: 'Today',
      time: '12:30 PM',
      totalCalories: 650,
      itemsCount: 4,
    },
    {
      id: '2',
      date: 'Today',
      time: '8:15 AM',
      totalCalories: 420,
      itemsCount: 3,
    },
    {
      id: '3',
      date: 'Yesterday',
      time: '7:45 PM',
      totalCalories: 780,
      itemsCount: 5,
    },
    {
      id: '4',
      date: 'Yesterday',
      time: '1:00 PM',
      totalCalories: 550,
      itemsCount: 3,
    },
  ];

  return (
    <div className="flex flex-col h-full bg-gray-50">
      {/* Header */}
      <div className="bg-white border-b px-6 py-4">
        <h1 className="text-gray-900">CaloriesAI</h1>
        <p className="text-gray-500 text-sm mt-1">Track your nutrition with AI</p>
      </div>

      {/* Action Buttons */}
      <div className="px-6 py-6 space-y-3">
        <Button
          className="w-full h-14 bg-blue-600 hover:bg-blue-700 text-white"
          onClick={() => onNavigate('add-photo')}
        >
          <Camera className="mr-2 h-5 w-5" />
          Take Photo
        </Button>
        <Button
          variant="outline"
          className="w-full h-14 border-gray-300"
          onClick={() => onNavigate('add-photo')}
        >
          <Image className="mr-2 h-5 w-5" />
          From Gallery
        </Button>
      </div>

      {/* Recent Analyses */}
      <div className="flex-1 px-6 pb-6">
        <h2 className="text-gray-900 mb-4">Recent Analyses</h2>
        <div className="space-y-3">
          {recentAnalyses.map((analysis) => (
            <Card
              key={analysis.id}
              className="p-4 bg-white border border-gray-200 cursor-pointer hover:shadow-md transition-shadow"
              onClick={() => onNavigate('result')}
            >
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 bg-gray-100 rounded-lg flex items-center justify-center">
                    <Clock className="h-5 w-5 text-gray-400" />
                  </div>
                  <div>
                    <p className="text-gray-900">{analysis.date}</p>
                    <p className="text-sm text-gray-500">{analysis.time}</p>
                  </div>
                </div>
                <div className="text-right">
                  <p className="text-gray-900">{analysis.totalCalories} kcal</p>
                  <p className="text-sm text-gray-500">{analysis.itemsCount} items</p>
                </div>
              </div>
            </Card>
          ))}
        </div>
      </div>
    </div>
  );
}
