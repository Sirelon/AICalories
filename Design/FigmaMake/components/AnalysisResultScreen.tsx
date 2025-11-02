import { ArrowLeft, Check } from 'lucide-react';
import { Button } from './ui/button';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from './ui/table';

interface FoodItem {
  name: string;
  weight: string;
  calories: number;
}

interface AnalysisResultScreenProps {
  onNavigate: (screen: string) => void;
}

export function AnalysisResultScreen({ onNavigate }: AnalysisResultScreenProps) {
  const detectedFoods: FoodItem[] = [
    { name: 'Grilled Chicken', weight: '150g', calories: 248 },
    { name: 'Brown Rice', weight: '100g', calories: 112 },
    { name: 'Broccoli', weight: '80g', calories: 27 },
    { name: 'Olive Oil', weight: '10g', calories: 88 },
  ];

  const totalCalories = detectedFoods.reduce((sum, item) => sum + item.calories, 0);

  return (
    <div className="flex flex-col h-full bg-gray-50">
      {/* Header */}
      <div className="bg-white border-b px-6 py-4">
        <div className="flex items-center gap-3">
          <button
            onClick={() => onNavigate('add-photo')}
            className="p-1 -ml-1 text-gray-600 hover:text-gray-900"
          >
            <ArrowLeft className="h-6 w-6" />
          </button>
          <div>
            <h1 className="text-gray-900">Analysis Result</h1>
            <p className="text-gray-500 text-sm mt-1">Review detected items</p>
          </div>
        </div>
      </div>

      {/* Content */}
      <div className="flex-1 px-6 py-6 space-y-6 overflow-auto">
        {/* Image Preview */}
        <div>
          <label className="block text-gray-700 mb-3">Photo</label>
          <div className="aspect-video w-full bg-gray-200 rounded-lg overflow-hidden">
            <div className="w-full h-full flex items-center justify-center text-gray-400">
              Image Preview
            </div>
          </div>
        </div>

        {/* Results Table */}
        <div>
          <label className="block text-gray-700 mb-3">Detected Foods</label>
          <div className="bg-white rounded-lg border border-gray-200 overflow-hidden">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Food</TableHead>
                  <TableHead className="text-center">Weight</TableHead>
                  <TableHead className="text-right">Calories</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {detectedFoods.map((food, index) => (
                  <TableRow key={index}>
                    <TableCell>{food.name}</TableCell>
                    <TableCell className="text-center">{food.weight}</TableCell>
                    <TableCell className="text-right">{food.calories} kcal</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
          
          {/* Total */}
          <div className="mt-4 p-4 bg-blue-50 rounded-lg border border-blue-200">
            <div className="flex justify-between items-center">
              <span className="text-gray-700">Total Calories</span>
              <span className="text-blue-700">{totalCalories} kcal</span>
            </div>
          </div>
        </div>
      </div>

      {/* Bottom Button */}
      <div className="px-6 py-4 bg-white border-t">
        <Button
          className="w-full h-14 bg-green-600 hover:bg-green-700 text-white"
          onClick={() => onNavigate('home')}
        >
          <Check className="mr-2 h-5 w-5" />
          Confirm & Save
        </Button>
      </div>
    </div>
  );
}
