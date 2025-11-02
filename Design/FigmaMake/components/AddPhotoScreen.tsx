import { Plus, ArrowLeft } from 'lucide-react';
import { Button } from './ui/button';
import { Textarea } from './ui/textarea';
import { useState } from 'react';

interface AddPhotoScreenProps {
  onNavigate: (screen: string) => void;
}

export function AddPhotoScreen({ onNavigate }: AddPhotoScreenProps) {
  const [description, setDescription] = useState('');

  return (
    <div className="flex flex-col h-full bg-gray-50">
      {/* Header */}
      <div className="bg-white border-b px-6 py-4">
        <div className="flex items-center gap-3">
          <button
            onClick={() => onNavigate('home')}
            className="p-1 -ml-1 text-gray-600 hover:text-gray-900"
          >
            <ArrowLeft className="h-6 w-6" />
          </button>
          <div>
            <h1 className="text-gray-900">Add Photos</h1>
            <p className="text-gray-500 text-sm mt-1">Upload 1-3 images</p>
          </div>
        </div>
      </div>

      {/* Content */}
      <div className="flex-1 px-6 py-6 space-y-6 overflow-auto">
        {/* Image Placeholders */}
        <div>
          <label className="block text-gray-700 mb-3">Photos</label>
          <div className="grid grid-cols-3 gap-3">
            {[1, 2, 3].map((index) => (
              <button
                key={index}
                className="aspect-square border-2 border-dashed border-gray-300 rounded-lg bg-white hover:border-blue-400 hover:bg-blue-50 transition-colors flex items-center justify-center"
              >
                <Plus className="h-8 w-8 text-gray-400" />
              </button>
            ))}
          </div>
          <p className="text-sm text-gray-500 mt-2">Tap to add images</p>
        </div>

        {/* Description Field */}
        <div>
          <label className="block text-gray-700 mb-2">
            Add description <span className="text-gray-400">(optional)</span>
          </label>
          <Textarea
            placeholder="e.g., Breakfast at home, restaurant meal..."
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            className="min-h-24 resize-none"
          />
        </div>
      </div>

      {/* Bottom Button */}
      <div className="px-6 py-4 bg-white border-t">
        <Button
          className="w-full h-14 bg-blue-600 hover:bg-blue-700 text-white"
          onClick={() => onNavigate('result')}
        >
          Analyze
        </Button>
      </div>
    </div>
  );
}
